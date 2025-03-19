package com.fis.epm.threads;

public abstract class AbstractThread {
	private int miThreadState = 1;
	private long threadId;
	private int miDelayTime = 100;

	//
	public long getThreadId() {
		return this.threadId;
	}

	public int getMiDelayTime() {
		return miDelayTime;
	}

	public void setMiDelayTime(int miDelayTime) {
		this.miDelayTime = miDelayTime;
	}

	// function
	public void start() {
		miThreadState = 1;
		Runnable r = new Runnable() {
			@Override
			public void run() {
				runThread();
			}
		};
		Thread t = new Thread(r);
		t.start();
	}

	public void stop() {
		miThreadState = 0;
	}

	public boolean isRunningThread() {
		return this.miThreadState != 0;
	}

	// abstract
	protected abstract void init() throws Exception;

	protected abstract void process() throws Exception;

	protected abstract void end() throws Exception;

	// run thread
	private void runThread() {
		while (isRunningThread()) {
			try {
				init();
				process();
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				try {
					end();
				} catch (Exception exp) {
					exp.printStackTrace();
				} finally {

				}
			}
			try {
				Thread.sleep(this.miDelayTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
