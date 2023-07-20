package com.bsren.qiu.collections;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedBlockingDeque<E> extends AbstractQueue<E> implements BlockingDeque<E>, Serializable {

	private static final long serialVersionUID = -387911632671998426L;

	static final class Node<E> {
		E item;

		Node<E> prev;

		Node<E> next;

		Node(E x) {
			item = x;
		}
	}

	transient Node<E> first;

	transient Node<E> last;

	private transient int count;

	private final int capacity;

	final ReentrantLock lock = new ReentrantLock();

	private final Condition notEmpty = lock.newCondition();

	private final Condition notFull = lock.newCondition();

	public LinkedBlockingDeque() {
		this(Integer.MAX_VALUE);
	}

	public LinkedBlockingDeque(int capacity) {
		this.capacity = capacity;
	}

	//basic linking and unlinking
	private boolean linkFirst(Node<E> node) {
		if (count >= capacity) {
			return false;
		}
		Node<E> f = first;
		node.next = f;
		first = node;
		if (last == null) {
			last = node;
		} else {
			f.prev = node;
		}
		count++;
		notEmpty.signal();
		return true;
	}

	private boolean linkLast(Node<E> node) {
		if (count > capacity) {
			return false;
		}
		Node<E> l = last;
		node.prev = l;
		l = node;
		if (first == null) {
			first = node;
		} else {
			l.next = node;
		}
		count++;
		notEmpty.signal();
		return true;
	}

	@GuardBy("this")
	private E unlinkFirst() {
		Node<E> f = first;
		if (f == null) {
			return null;
		}
		Node<E> n = f.next;
		E item = f.item;
		f.item = null;
		f.next = f;
		first = n;
		if (n == null) {
			last = null;
		} else {
			n.prev = null;
		}
		count--;
		notFull.signal();
		return item;
	}

	private E unlinkLast(){
		Node<E> l = last;
		if(l==null){
			return null;
		}
		Node<E> p = l.prev;
		E item = l.item;
		l.item = null;
		l.prev = l;
		last = p;
		if(p==null){
			first = null;
		}else {
			p.next = null;
		}
		count--;
		notFull.signal();
		return item;
	}

	void unlink(Node<E> x){
		Node<E> p = x.prev;
		Node<E> n = x.next;
		if(p==null){
			unlinkFirst();
		}else if(n==null){
			unlinkLast();
		}else {
			p.next = n;
			n.prev = p;
			x.item = null;
			// Don't mess with x's links.  They may still be in use by
			// an iterator.
			count--;
			notFull.signal();
		}
	}

	public boolean offerFirst(E e){
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return linkFirst(node);
		}finally {
			lock.unlock();
		}
	}

	public boolean offerLast(E e){
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return linkLast(node);
		}finally {
			lock.unlock();
		}
	}

	public void addFirst(E e){
		if(!offerFirst(e)){
			throw new IllegalStateException("Deque full");
		}
	}

	public void addLast(E e){
		if(!offerLast(e)){
			throw new IllegalStateException("Deque full");
		}
	}

	public void putFirst(E e) throws InterruptedException{
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			while (!linkFirst(node)){
				notFull.await();
			}
		}finally {
			lock.unlock();
		}
	}

	public void putLast(E e) throws InterruptedException{
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		final ReentrantLock lock= this.lock;
		lock.lock();
		try {
			while (!linkLast(node)){
				notFull.await();
			}
		}finally {
			lock.unlock();
		}
	}

	public boolean offerFirst(E e, long timeout, TimeUnit timeUnit) throws InterruptedException{
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		long nanos = timeUnit.toNanos(timeout);
		ReentrantLock lock = this.lock;
		lock.lock();
		try {
			while (!linkFirst(node)){
				if(nanos<=0){
					return false;
				}
				nanos = notFull.awaitNanos(nanos);
			}
			return true;
		}finally {
			lock.unlock();
		}
	}

	public boolean offerLast(E e, long timeout,TimeUnit timeUnit) throws InterruptedException{
		if(e==null){
			throw new NullPointerException();
		}
		Node<E> node = new Node<>(e);
		long nanos = timeUnit.toNanos(timeout);
		ReentrantLock lock = this.lock;
		lock.lock();
		try {
			while (!linkLast(node)){
				if(nanos<0){
					return false;
				}
				nanos = notFull.awaitNanos(nanos);
			}
			return true;
		}finally {
			lock.unlock();
		}
	}

	public E removeFirst(){
		E x = pollFirst();
		if(x==null){
			throw new NoSuchElementException();
		}
		return x;
	}

	public E removeLast(){
		E x = pollLast();
		if(x==null){
			throw new NoSuchElementException();
		}
		return x;
	}

	@Override
	public E pollFirst() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return unlinkFirst();
		}finally {
			lock.unlock();
		}
	}

	@Override
	public E pollLast() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return unlinkLast();
		}finally {
			lock.unlock();
		}
	}

	@Override
	public E takeFirst() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			E x;
			while ((x = unlinkFirst())==null){
				notEmpty.await();
			}
			return x;
		}finally {
			lock.unlock();
		}
	}

	@Override
	public E takeLast() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			E x;
			while ((x = unlinkLast())==null){
				notEmpty.await();
			}
			return x;
		}finally {
			lock.unlock();
		}
	}

	public E pollFirst(long timeout, TimeUnit unit)
			throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			E x;
			while ( (x = unlinkFirst()) == null) {
				if (nanos <= 0)
					return null;
				nanos = notEmpty.awaitNanos(nanos);
			}
			return x;
		} finally {
			lock.unlock();
		}
	}

	public E pollLast(long timeout, TimeUnit unit)
			throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			E x;
			while ( (x = unlinkLast()) == null) {
				if (nanos <= 0)
					return null;
				nanos = notEmpty.awaitNanos(nanos);
			}
			return x;
		} finally {
			lock.unlock();
		}
	}

	public E getFirst() {
		E x = peekFirst();
		if (x == null) throw new NoSuchElementException();
		return x;
	}

	/**
	 * @throws NoSuchElementException {@inheritDoc}
	 */
	public E getLast() {
		E x = peekLast();
		if (x == null) throw new NoSuchElementException();
		return x;
	}

	public E peekFirst() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (first == null) ? null : first.item;
		} finally {
			lock.unlock();
		}
	}

	public E peekLast() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (last == null) ? null : last.item;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		if(o==null){
			return false;
		}
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			for (Node<E> p = first;p!=null;p = p.next){
				if(o.equals(p)){
					unlink(p);
					return true;
				}
			}
			return false;
		}finally {
			lock.unlock();
		}
	}


}
