package rest.patch;

public class OperationDTO {
	
	private OperationType op;
	private String path;
	private String value;
	
	public OperationType getOp() {
		return op;
	}
	
	public void setOp(OperationType op) {
		this.op = op;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
