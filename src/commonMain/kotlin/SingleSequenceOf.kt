package singlesequenceof

fun <T> singleSequenceOf(element: T): Sequence<T> = Sequence {
    object : Iterator<T> {
        private var _hasNext: Boolean = true

        override fun next(): T {
            if (!_hasNext) throw NoSuchElementException()
            _hasNext = false
            return element
        }

        override fun hasNext(): Boolean = _hasNext
    }
}