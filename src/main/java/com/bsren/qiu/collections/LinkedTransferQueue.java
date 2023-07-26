package com.bsren.qiu.collections;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.locks.LockSupport;


public class LinkedTransferQueue<E> extends AbstractQueue<E> implements TransferQueue<E> , Serializable {

	private static final long serialVersionUID = -3223113410248163686L;

	private static final boolean MP =
			Runtime.getRuntime().availableProcessors() > 1;

	@Override
	public Iterator<E> iterator() {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean tryTransfer(E e) {
		return false;
	}

	@Override
	public void transfer(E e) throws InterruptedException {

	}

	@Override
	public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public boolean hasWaitingConsumer() {
		return false;
	}

	@Override
	public int getWaitingConsumerCount() {
		return 0;
	}

	@Override
	public void put(E e) throws InterruptedException {

	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public E take() throws InterruptedException {
		return null;
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return null;
	}

	@Override
	public int remainingCapacity() {
		return 0;
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		return 0;
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		return 0;
	}

	@Override
	public boolean offer(E e) {
		return false;
	}

	@Override
	public E poll() {
		return null;
	}

	@Override
	public E peek() {
		return null;
	}


	static final class Node{
		final boolean isData;  //false if this is a request node
		volatile Object item;
		volatile Node next;
		volatile Thread waiter;

		boolean casNext(Node cmp,Node val){
			return UNSAFE.compareAndSwapObject(this,nextOffset,cmp,val);
		}

		boolean casItem(Object cmp,Object val){
			return UNSAFE.compareAndSwapObject(this,itemOffset,cmp,val);
		}

		Node(Object item, boolean isData){
			UNSAFE.putObject(this,itemOffset,item);
			this.isData = isData;
		}

		void forgetNext(){
			UNSAFE.putObject(this,nextOffset,this);
		}


		/**
		 * Sets item to self and waiter to null, to avoid garbage retention after matching or cancelling.
		 * Uses relaxed writes because order is already constrained in the only calling contexts:
		 * item is forgotten only after volatile/atomic mechanics that extract items.
		 * Similarly, clearing waiter follows either CAS or return from park (if ever parked; else we don't care).
		 */
		void forgetContents(){
			UNSAFE.putObject(this,itemOffset,this);
			UNSAFE.putObject(this,waiterOffset,this);
		}

		boolean isMatched(){
			Object x = item;
			return (x==this) || ((x==null)==isData);
		}

		boolean isUnmatchedRequest() {
			return !isData && item == null;
		}

		boolean cannotPrecede(boolean haveData) {
			boolean d = isData;
			Object x;
			return d != haveData && (x = item) != this && (x != null) == d;
		}

		boolean tryMatchData() {
			// assert isData;
			Object x = item;
			if (x != null && x != this && casItem(x, null)) {
				LockSupport.unpark(waiter);
				return true;
			}
			return false;
		}




		private static final long serialVersionUID = -3375979862319811754L;


		// Unsafe mechanics
		private static final sun.misc.Unsafe UNSAFE;
		private static final long itemOffset;
		private static final long nextOffset;
		private static final long waiterOffset;
		static {
			try {
				UNSAFE = sun.misc.Unsafe.getUnsafe();
				Class<?> k = Node.class;
				itemOffset = UNSAFE.objectFieldOffset
						(k.getDeclaredField("item"));
				nextOffset = UNSAFE.objectFieldOffset
						(k.getDeclaredField("next"));
				waiterOffset = UNSAFE.objectFieldOffset
						(k.getDeclaredField("waiter"));
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	transient volatile Node head;

	transient volatile Node tail;

	// CAS methods for fields
	private boolean casTail(Node cmp,Node val) {
		return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
	}

	private boolean casHead(Node cmp, Node val) {
		return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
	}

	private boolean casSweepVotes(int cmp, int val) {
		return UNSAFE.compareAndSwapInt(this, sweepVotesOffset, cmp, val);
	}

	/*
	 * Possible values for "how" argument in xfer method.
	 */
	private static final int NOW   = 0; // for untimed poll, tryTransfer
	private static final int ASYNC = 1; // for offer, put, add
	private static final int SYNC  = 2; // for transfer, take
	private static final int TIMED = 3; // for timed poll, tryTransfer

	private static final sun.misc.Unsafe UNSAFE;
	private static final long headOffset;
	private static final long tailOffset;
	private static final long sweepVotesOffset;
	static {
		try {
			UNSAFE = sun.misc.Unsafe.getUnsafe();
			Class<?> k = java.util.concurrent.LinkedTransferQueue.class;
			headOffset = UNSAFE.objectFieldOffset
					(k.getDeclaredField("head"));
			tailOffset = UNSAFE.objectFieldOffset
					(k.getDeclaredField("tail"));
			sweepVotesOffset = UNSAFE.objectFieldOffset
					(k.getDeclaredField("sweepVotes"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
