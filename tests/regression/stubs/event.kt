package audit
class Event<T> {val callbacks=mutableListOf<T>();fun register(callback:T){callbacks.add(callback)}}
