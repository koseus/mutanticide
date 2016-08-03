package com.ibm.contest;

import java.util.*;
import java.io.*;
import java.net.*;
import java.math.*;

public class King {




  public static void startRunningThread( Thread newThread ) {




}

  public static void startRun() {
  }

  public static void endRunningThread() {
  }

  public static void closeConcurrentEvent(String programLocation){
   }




  public static void beforeConcurrentEvent(String programLocation, int concurrentEventType) {

	//System.out.println("before concurrent event!!!!!");


}

  public static void beforeConcurrentEvent(Object contextObject, String programLocation, int concurrentEventType) {


}

  public static void beforeConcurrentEvent(String varName, String programLocation, int concurrentEventType) {

}

  public static void beforeConcurrentEvent(Object varReference, String varName, String programLocation, int type)  {


}

  public static void afterConcurrentEvent(String programLocation, int concurrentEventType) {

}

  public static void afterConcurrentEvent(Object contextObject,String programLocation, int concurrentEventType){
  }

  public static void afterConcurrentEvent(Object value,String varName,String programLocation, int concurrentEventType) {

}

  public static void afterConcurrentEvent(Object instance,Object value,String varName,String programLocation, int concurrentEventType) {


}

  public static void arrayVariableAccess(Object variableRef, String variableName, String variableType, String programLocation){
  }

  public static void arrayVariableAccess(Object instance, Object variableRef, String variableName, String variableType, String programLocation){
  }

  public static void arrayWriteAccess(Object arrayRef, int arrayIndex, Object valueWritten, String programLocation){
  }

  public static void ct_Thread_interrupt(Thread t) throws SecurityException {
    t.interrupt();
  }

 // public static void ct_ThreadGroup_interrupt(ThreadGroup g) throws SecurityException {
 //   g.interrupt();
 // }

  public static boolean ct_Thread_isInterrupted(Thread t){
    return t.isInterrupted();
  }

  public static boolean ct_Thread_interrupted() {
    return Thread.interrupted();
  }

  public static void ct_Thread_join(Thread t) throws InterruptedException {
    t.join();
  }

  public static void ct_Thread_join(Thread t, long millis) throws InterruptedException {
    t.join(millis);
  }

  public static void ct_Thread_join(Thread t, long millis, int nanos) throws InterruptedException {
    t.join(millis, nanos);
  }

  public static void ct_Thread_sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  public static void ct_Thread_sleep(long millis, int nanos) throws InterruptedException {
    Thread.sleep(millis, nanos);
  }

  public static void ct_Object_wait(Object o) throws InterruptedException {
    o.wait();

//System.out.println("before wait!!!!!!!!!!!!!!!!!!!!!!");


}

  public static void ct_Object_wait(Object o, long millis) throws InterruptedException {
    o.wait(millis);
  }

  public static void ct_Object_wait(Object o, long millis, int nanos) throws InterruptedException {
    o.wait(millis, nanos);
  }

  public static  Socket ct_ServerSocket_accept(ServerSocket caller) throws IOException{
    return caller.accept();
  }

  public static Socket ct_Socket_constructor(final InetAddress address, final int port, final InetAddress localAdd,
                                                                              final int localPort) throws IOException {
    return new Socket(address, port, localAdd, localPort);
  }

  public static Socket ct_Socket_constructor(final InetAddress address, final int port) throws IOException {
     return new Socket(address, port);
  }

  public static Socket ct_Socket_constructor(final String address, final int port, final InetAddress localAdd, final int localPort)
                                                                                          throws IOException {
    return new Socket(address, port, localAdd, localPort);
  }

  public static Socket ct_Socket_constructor(final String address, final int port) throws IOException {
    return new Socket(address, port);
  }

  public static InputStream ct_Socket_getInputStream(Socket caller) throws IOException {
    return caller.getInputStream();
  }


  public static OutputStream ct_Socket_getOutputStream(Socket caller) throws IOException {
      return caller.getOutputStream();
  }

  public static int ct_Socket_getSoLinger(Socket caller) throws SocketException {
     return caller.getSoLinger();
  }

  public static void ct_Socket_setSoLinger( Socket caller, boolean on, int linger ) throws SocketException {
    caller.setSoLinger(on, linger);
  }


  public static String[] startMain(String[] args, String str){

  return args;
  }

  public static void conTestExit() {

 // System.out.println ("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee  " );
}

  public static void startStaticInitializer( String className ){
  }

  public static void endStaticInitializer(){
  }

   public static void branchPoint ( String programLocation ) {
    System.out.println ("branch");

   }

   public static void methodEntry ( String programLocation ) {
   System.out.println ("method");
   }

   public static void uncaughtException ( String programLocation ) {
   }
}

