class MethodXLine {
	private final String methodName;
	private final int lineNumber;

	public MethodXLine(String name, int line){
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
