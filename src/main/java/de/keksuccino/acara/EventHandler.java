
//Acara - Simple Java Event System

//Copyright (c) 2020-2023 Keksuccino.
//Acara is licensed under MIT.

package de.keksuccino.acara;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class handles all events and event listeners of your project.<br>
 * You should only create ONE instance per project.
 **/
@SuppressWarnings("unchecked")
public class EventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<String, List<ListenerContainer>> events = new HashMap<>();

	/**
	 * Call this when all registered event listeners for the given event type should get invoked/notified.<br><br>
	 *
	 * <b>Usage:</b>
	 *
	 * <pre>
	 * {@code ExampleEvent e = new ExampleEvent();}
	 * {@code EventHandler.post(e);}
	 * </pre>
	 */
	public void postEvent(EventBase event) {
		if (eventsExistForType(event.getClass())) {
			List<ListenerContainer> highest = new ArrayList<>();
			List<ListenerContainer> high = new ArrayList<>();
			List<ListenerContainer> normal = new ArrayList<>();
			List<ListenerContainer> low = new ArrayList<>();
			List<ListenerContainer> lowest = new ArrayList<>();
			for (ListenerContainer c : events.get(event.getClass().getName())) {
				if (c.priority == EventPriority.HIGHEST) {
					highest.add(c);
				}
				if (c.priority == EventPriority.HIGH) {
					high.add(c);
				}
				if (c.priority == EventPriority.NORMAL) {
					normal.add(c);
				}
				if (c.priority == EventPriority.LOW) {
					low.add(c);
				}
				if (c.priority == EventPriority.LOWEST) {
					lowest.add(c);
				}
			}
			for (ListenerContainer c : highest) {
				c.noticeListener(event);
			}
			for (ListenerContainer c : high) {
				c.noticeListener(event);
			}
			for (ListenerContainer c : normal) {
				c.noticeListener(event);
			}
			for (ListenerContainer c : low) {
				c.noticeListener(event);
			}
			for (ListenerContainer c : lowest) {
				c.noticeListener(event);
			}
		}
	}

	/**
	 * This will register all public static event listener methods of the given class annotated with {@link SubscribeEvent}.<br>
	 * Event listener methods need to have only ONE parameter and this parameter has to be the event type (subclass of {@link EventBase}).<br><br>
	 *
	 * <b>Example of a valid event listener method:</b>
	 *
	 * <pre>
	 * {@code @SubscribeEvent(priority = EventPriority.NORMAL)}
	 * {@code public static void onEvent(EventBase event)} {
	 *    //do something with the event object
	 * }
	 * </pre>
	 **/
	public void registerListenersOf(Class<?> clazz) {
		this.registerListenerMethods(this.getEventMethodsOfClass(clazz));
	}

	/**
	 * This will register all public (static and non-static) event listener methods of the given object annotated with {@link SubscribeEvent}.<br>
	 * Event listener methods need to have only ONE parameter and this parameter has to be the event type (subclass of {@link EventBase}).<br><br>
	 *
	 * <b>Example of a valid event listener method:</b>
	 *
	 * <pre>
	 * {@code @SubscribeEvent(priority = EventPriority.NORMAL)}
	 * {@code public void onEvent(EventBase event)} {
	 *    //do something with the event object
	 * }
	 * </pre>
	 **/
	public void registerListenersOf(Object object) {
		this.registerListenerMethods(this.getEventMethodsOfObject(object));
	}

	protected void registerListenerMethods(List<EventMethod> methods) {
		for (EventMethod m : methods) {
			Consumer<EventBase> listener = (event) -> {
				try {
					m.method.invoke(m.parent, event);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
			ListenerContainer container = new ListenerContainer(m.eventType.getName(), listener, m.priority);
			this.registerListener(container);
		}
	}

	@NotNull
	protected List<EventMethod> getEventMethodsOfClass(Class<?> c) {
		List<EventMethod> l = new ArrayList<>();
		try {
			if (c != null) {
				for (Method m : c.getMethods()) {
					if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers()) && this.isEventMethod(m)) {
						Class<? extends EventBase> eventClass = (Class<? extends EventBase>) m.getParameterTypes()[0];
						l.add(new EventMethod(m, c, eventClass, true));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

	@NotNull
	protected List<EventMethod> getEventMethodsOfObject(Object object) {
		List<EventMethod> l = new ArrayList<>();
		try {
			if (object != null) {
				Class<?> c = object.getClass();
				for (Method m : c.getMethods()) {
					if (Modifier.isPublic(m.getModifiers()) && this.isEventMethod(m)) {
						Class<? extends EventBase> eventClass = (Class<? extends EventBase>) m.getParameterTypes()[0];
						l.add(new EventMethod(m, object, eventClass, Modifier.isStatic(m.getModifiers())));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

	public void registerListener(Consumer<EventBase> listener, Class<? extends EventBase> eventClass) {
		this.registerListener(listener, eventClass, EventPriority.NORMAL);
	}

	public void registerListener(Consumer<EventBase> listener, Class<? extends EventBase> eventClass, EventPriority priority) {
		this.registerListener(new ListenerContainer(eventClass.getName(), listener, priority));
	}

	protected void registerListener(ListenerContainer listenerContainer) {
		try {
			if (!eventsExistForIdentifier(listenerContainer.eventIdentifier)) {
				events.put(listenerContainer.eventIdentifier, new ArrayList<>());
			}
			events.get(listenerContainer.eventIdentifier).add(listenerContainer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean isEventMethod(Method m) {
		if (m != null) {
			try {
				return (m.isAnnotationPresent(SubscribeEvent.class) && this.hasEventMethodHeader(m));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	protected boolean hasEventMethodHeader(Method m) {
		if (m != null) {
			Class<?>[] paramTypes = m.getParameterTypes();
			return ((paramTypes.length == 1) && EventBase.class.isAssignableFrom(paramTypes[0]));
		}
		return false;
	}
	
	public boolean eventsExistForType(Class<? extends EventBase> listenerType) {
		if (listenerType == null) {
			return false;
		}
		return this.eventsExistForIdentifier(listenerType.getName());
	}

	public boolean eventsExistForIdentifier(String identifier) {
		if (identifier == null) {
			return false;
		}
		return (events.get(identifier) != null);
	}

	/** Deprecated! Use {@link EventHandler#postEvent(EventBase)} instead! **/
	@Deprecated
	public void callEventsFor(EventBase event) {
		this.postEvent(event);
	}

	/** Deprecated! Use {@link EventHandler#registerListenersOf(Object)} instead! **/
	@Deprecated
	public void registerEventsFrom(Object object) {
		this.registerListenersOf(object);
	}

	/** Deprecated! Use {@link EventHandler#registerListenersOf(Class)} instead! **/
	@Deprecated
	public void registerEventsFrom(Class<?> c) {
		this.registerListenersOf(c);
	}

	public static class ListenerContainer {

		public final Consumer<EventBase> listener;
		public final String eventIdentifier;
		public final EventPriority priority;

		public ListenerContainer(String eventIdentifier, Consumer<EventBase> listener, EventPriority priority) {
			this.listener = listener;
			this.eventIdentifier = eventIdentifier;
			this.priority = priority;
		}

		public void noticeListener(EventBase event) {
			try {
				this.listener.accept(event);
			} catch (Exception e) {
				LOGGER.error("##################################");
				LOGGER.error("[ACARA] Failed to notify event listener!");
				LOGGER.error("[ACARA] Event Name: " + this.eventIdentifier);
				LOGGER.error("##################################");
				e.printStackTrace();
			}
		}

	}

	protected static class EventMethod {

		public final Method method;
		public final Object parent;
		public final EventPriority priority;
		public final Class<? extends EventBase> eventType;
		public final boolean isStatic;

		public EventMethod(Method method, Object parent, Class<? extends EventBase> eventType, boolean isStatic) {
			this.method = method;
			this.parent = parent;
			this.eventType = eventType;
			this.priority = method.getAnnotation(SubscribeEvent.class).priority();
			this.isStatic = isStatic;
		}

	}

}
