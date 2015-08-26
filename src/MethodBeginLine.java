class MethodBeginLine {
	private final String methodName;
	private int beginLine;

	public MethodBeginLine(String name, int line){
		methodName = name;
		beginLine = line;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getBeginLine(){
		return beginLine;
	}

	public void setBeginLine(int line){
		beginLine = line;
	}
}
