class SynchronizedBlock {
	private final String methodName;
	private final int lineNumber;

	public SynchronizedBlock(String name, int line){
		methodName = name;
		lineNumber = line;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getLineNumber(){
		return lineNumber;
	}
}
