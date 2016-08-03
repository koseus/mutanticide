package examples.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Company {

	String name;
	boolean active = true;
	Company parent;
	List children = new ArrayList();
	
	public Company(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name 
			//+ " " + (active?"A":"I")
			//+ " (" + parent + ")"
			;
	}
	
	public synchronized boolean isActive() {
		return active;
	}
	
	public synchronized boolean setParent(Company newParent) {
		if(newParent == null) {
			this.parent = null;
			return true;
		} else if(newParent.isActive() 
				|| !this.isActive()) {
			newParent.children.add(this);
			if(parent!=null) {
				parent.children.remove(this);
			}			
			parent = newParent;
			return true;
		} else {
			return false;		
    	}
	}
	
	public synchronized boolean setActive() {
		if(parent == null || parent.isActive()) {
			active = false;
			return true;
		} else {
			return false;
		}			
	}
	synchronized boolean isChildrenInactive() {
		for(Iterator i=children.iterator(); i.hasNext();) {
			Company child = (Company)i.next();
			if(child.isActive())
				return false;
		}
		return true;
	}
	
	public synchronized boolean setInactive() {
		if(isChildrenInactive()) {
			active = false;
			return true;			
		} else {
			return false;
		}
	}
	
	static Company clients[] = new Company[] {
		new Company("Rosneft"), new Company("Gazprom")	
	};
	
	public static void main(String[] args) {
		Thread t1 =  new Thread() {
			public void run() {
				clients[0].setParent(clients[1]);
			}
		};
		Thread t2 =  new Thread() {
			public void run() {
				clients[1].setInactive();
			}
		};
		
		t2.start();
		t1.start();
		
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//System.out.println(clients[0] + "\n" + clients[1]);
		
		boolean b = clients[0].parent == null 
			|| clients[1].isActive() 
			|| !clients[0].isActive();
		System.out.println(b);
		assert b; 
	}
}
