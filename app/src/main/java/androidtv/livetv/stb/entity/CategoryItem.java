package androidtv.livetv.stb.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

import static androidtv.livetv.stb.utils.LinkConfig.CATEGORY_TABLE;

@Generated("com.robohorse.robopojogenerator")
@Entity(tableName = CATEGORY_TABLE	,indices = {@Index("category_id")})
public class CategoryItem{

	@Ignore
	@SerializedName("updated_at")
	private String updatedAt;

	@Ignore
	@SerializedName("display_order")
	private int displayOrder;

	@Ignore
	@SerializedName("description")
	private String description;

	@Ignore
	@SerializedName("created_at")
	private String createdAt;

	@PrimaryKey
	@ColumnInfo(name = "category_id")
	@SerializedName("id")
	private int id;

	@ColumnInfo(name = "category_name")
	@SerializedName("title")
	private String title;

	@Ignore
	@SerializedName("status")
	private String status;

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public void setDisplayOrder(int displayOrder){
		this.displayOrder = displayOrder;
	}

	public int getDisplayOrder(){
		return displayOrder;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"CategoryItem{" + 
			"updated_at = '" + updatedAt + '\'' + 
			",display_order = '" + displayOrder + '\'' + 
			",description = '" + description + '\'' + 
			",created_at = '" + createdAt + '\'' + 
			",id = '" + id + '\'' + 
			",title = '" + title + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}