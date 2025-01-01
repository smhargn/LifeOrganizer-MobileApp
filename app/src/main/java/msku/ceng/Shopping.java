package msku.ceng;

import java.util.Date;

public class Shopping {
    private String id;
    private String listName;
    private String productName;
    private Date createdDate;
    private boolean isChecked;

    public Shopping(String listName, String productName, Date createdDate) {
        this.id = java.util.UUID.randomUUID().toString();
        this.listName = listName;
        this.productName = productName;
        this.createdDate = createdDate;
        this.isChecked = false;
    }

    // Getters and Setters
    public String getId() { return id; }

    public String getListName() { return listName; }
    public void setListName(String listName) { this.listName = listName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}