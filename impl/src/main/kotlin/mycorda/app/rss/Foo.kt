package mycorda.app.rss

class Foo<T> () {

    fun wait ( func : () -> T ) :T  {
        return func.invoke()
    }

}