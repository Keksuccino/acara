# About
Acara is a simple and lightweight event system for Java.

# Usage
Acara is really easy to use and pretty much self-explanatory.

```java
public class Acara {

    //Every project should have its own EventHandler instance
    public static final EventHandler EVENT_HANDLER = new EventHandler();
    
    public static void main(String[] args) {
        
        //This will automatically register all public NON-STATIC event listener methods of the given object.
        EVENT_HANDLER.registerListenersOf(new Acara());
        
        //This will automatically register all public STATIC event listener methods of the given class.
        EVENT_HANDLER.registerListenersOf(Acara.class);
        
    }

    //When this method gets called, the event in it will get posted to the handler
    public static void someMethod() {
        //ExampleEvent extends EventBase
        ExampleEvent event = new ExampleEvent("some string parameter");
        //This will post the event to the handler, so all registered listeners for the posted event type get notified/invoked
        EVENT_HANDLER.postEvent(event);
    }

    //This is a NON-STATIC event listener method.
    //Registering this listener to the handler will make it listen for the event type it has as parameter.
    //Listener methods need to have the @EventListener annotation and need to have exactly one parameter, which is the event type it listens to.
    //When an event of the ExampleEvent type gets posted to the handler, this listener gets notified/invoked.
    @EventListener
    public void someEventListener(ExampleEvent event) {
        System.out.println(event.someString);
    }

    //This is a STATIC event listener method.
    //In this example, a priority was specified for the listener. The listener with the highest priority (per event type) gets notified/invoked first.
    @EventListener(priority = 2)
    public static void someStaticEventListener(ExampleEvent event) {
        System.out.println(event.someString);
    }

    //This is an example event class.
    //Events need to extend the EventBase class.
    public static class ExampleEvent extends EventBase {

        public final String someString;

        public ExampleEvent(String someString) {
            this.someString = someString;
        }

        //If an event is cancelable, you need to handle its possible cancellation when posting it to the handler.
        @Override
        public boolean isCancelable() {
            return false;
        }

    }

}
```

# Dependencies
Acara depends on [Log4j](https://github.com/apache/logging-log4j2).

# License
Acara is licensed under MIT.
See `LICENSE` for more information.

# Copyright
Acara Copyright © 2023 Keksuccino.
