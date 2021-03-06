package edu.harvard.i2b2.pm.services;

public class RegisteredCellParam {
    // every persistent object needs an identifier
    private String oid = new String();
    private String cellid = new String(); 
    private String name = new String();
    private String value = new String();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getCellid() {
		return cellid;
	}
	public void setCellid(String cellid) {
		this.cellid = cellid;
	}
}
