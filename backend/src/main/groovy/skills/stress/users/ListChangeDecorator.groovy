/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.stress.users

class ListChangeDecorator<T> implements List<T> {

    private List<T> decorated

    List<AddedListener<T>> addedListeners = []
    List<RemovedListener<T>> removedListeners = []

    ListChangeDecorator(List<T> decorated) {
        this.decorated = decorated
    }

    @Override
    int size() {
        return decorated.size()
    }

    @Override
    boolean isEmpty() {
        return decorated.isEmpty()
    }

    @Override
    boolean contains(Object o) {
        return decorated.contains(o)
    }

    @Override
    Iterator<T> iterator() {
        return decorated.iterator()
    }

    @Override
    Object[] toArray() {
        return decorated.toArray()
    }

    @Override
    def <T1> T1[] toArray(T1[] a) {
        return decorated.toArray(a)
    }

    @Override
    boolean add(T t) {
        boolean added = decorated.add(t)
        if (added) {
            addedListeners?.each {
                it.itemAdded(t)
            }
        }
        return added
    }

    @Override
    boolean remove(Object o) {
        boolean removed = decorated.remove(o)
        if (removed) {
            removedListeners?.each {
                it.itemRemoved((T) o)
            }
        }
        return removed
    }

    @Override
    boolean containsAll(Collection<?> c) {
        return decorated.containsAll(c)
    }

    @Override
    boolean addAll(Collection<? extends T> c) {
        return decorated.addAll(c)
    }

    @Override
    boolean addAll(int index, Collection<? extends T> c) {
        return decorated.addAll(index, c)
    }

    @Override
    boolean removeAll(Collection<?> c) {
        boolean removed = decorated.removeAll(c)
        if (removed) {
            //don't know which items were removed and which weren't, have to check each one
            c?.each {
                if (!decorated.contains(it)) {
                    removedListeners?.each { RemovedListener removedListener ->
                        removedListener.itemRemoved(it)
                    }
                }
            }
        }

        return removed
    }

    @Override
    boolean retainAll(Collection<?> c) {
        //inefficent but we don't use this anywhere
        List<T> tmp = new ArrayList<T>(decorated)
        boolean changed = decorated.retainAll(c)
        if (changed) {
            tmp.each {
                if (!decorated.contains(it)) {
                    removedListeners?.each { RemovedListener removedListener ->
                        removedListener.itemRemoved(it)
                    }
                }
            }
        }
    }

    @Override
    void clear() {
        decorated.clear()
    }

    @Override
    T get(int index) {
        return decorated.get(index)
    }

    @Override
    T set(int index, T element) {
        T removed = decorated.set(index, element)
        removedListeners?.each {
            it.itemRemoved(removed)
        }

        addedListeners?.each {
            it.itemAdded(element)
        }

        return removed
    }

    @Override
    void add(int index, T element) {
        decorated.add(index, element)
        addedListeners?.each {
            it.itemAdded(element)
        }
    }

    @Override
    T remove(int index) {
        T removed =  decorated.remove(index)
        removedListeners?.each {
            it.itemRemoved(removed)
        }

        return removed
    }

    @Override
    int indexOf(Object o) {
        return decorated.indexOf(o)
    }

    @Override
    int lastIndexOf(Object o) {
        return decorated.indexOf(o)
    }

    @Override
    ListIterator<T> listIterator() {
        return decorated.listIterator()
    }

    @Override
    ListIterator<T> listIterator(int index) {
        return decorated.listIterator(index)
    }

    @Override
    List<T> subList(int fromIndex, int toIndex) {
        return decorated.subList(fromIndex, toIndex)
    }
}
