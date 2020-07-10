package rest;

public class PatchOperationDTO {
	
	private Operation op;
	private String path;
	private Object value;
	
	public Operation getOp() {
		return op;
	}
	
	public void setOp(Operation op) {
		this.op = op;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
