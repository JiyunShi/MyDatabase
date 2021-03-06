import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.*;
public class MyDB{
	static boolean DBwritable = true;

	MyBinaryTree<User> usersTree =  new MyBinaryTree<User>();
	MyBinaryTree<Db> dbsTree =  new MyBinaryTree<Db>();

	public MyDB(){
		
		User defaultUser = new User("admin", "admin", true);
		usersTree.add(defaultUser);
	}
	
	
	//for user login check, return null if no record find
	public User login(String name, String password){
		
		User temp = new User(name,password,false);
		User result = usersTree.search(temp);
		if(result==null) return null;
		if(result.password.equals(password)) {
			result.canLogin=false;
			return result;
		}
		else return null;

		/*
		 * method before adding binary tree
		 * for(User user: users){
			if(user.name.equals(name) &&user.password.equals(password)&&user.canLogin){
				user.canLogin =false;
				return user;
			}
		}
		return null;*/
	}
	
	
	
	public void addUser(User currentUser, User user, PrintWriter out){
		if(currentUser.isAdmin){
			boolean isExsit =false;
			if(usersTree.search(user)!=null){
				isExsit =true;
			}
			
			if(!isExsit){
				usersTree.add(user);
				out.println(ClientHandler.WELCOMEWORD+"added 1 new user successfully");
			}else out.println(ClientHandler.WELCOMEWORD+"user already exist");
		}else out.println(ClientHandler.WELCOMEWORD+"You are not admin");
	}


	
	public void deleteUser(User currentUser, String username, PrintWriter out) {
		
		if(currentUser.isAdmin){
			usersTree.remove(new User(username,"",false), out);
		}else out.println(ClientHandler.WELCOMEWORD+"You are not admin");
		
		
		
	}



	public void updateUser(User currentUser, String olduserName, User updatedUser, PrintWriter out) {
		
		if(currentUser.isAdmin){
			
			usersTree.update(new User(olduserName,"",false),updatedUser,out);
			
		}else out.println(ClientHandler.WELCOMEWORD+"You are not admin");
		
		
		
	}


	
	public void createDB(User currentUser, String name, PrintWriter out) {
		
		if(currentUser.isAdmin){
			
			Db temp = new Db(name);
			if(dbsTree.search(temp)==null){
				dbsTree.add(temp);
				out.println(ClientHandler.WELCOMEWORD+"database created successfully");
			}else{
				out.println(ClientHandler.WELCOMEWORD+"The db name is already existing, please change");
				;
			}
			
			/*
			 * before change dbs to BTS.
			 * for(Db db:dbs){
				if(db.dbName.equals(name)){
					out.println(ClientHandler.WELCOMEWORD+"The db name is already existing, please change");
					return;
				}
			}
			dbs.add(new Db(name));
			out.println(ClientHandler.WELCOMEWORD+"database created successfully");*/
		}else out.println(ClientHandler.WELCOMEWORD+"you are not admin");
		
	}


	
	public void deleteDB(User currentUser, String name, PrintWriter out) {
		if(currentUser.isAdmin){
			if(currentUser.selectedDBname.equals(name)){
				currentUser.selectedDBname=null;
				currentUser.selectedDB = null;
			}
			dbsTree.remove(new Db(name), out);
			
		}else out.println(ClientHandler.WELCOMEWORD+"not admin");
		
		
		
	}


	
	public UserDBAction useDB(String name, PrintWriter out) {
		Db temp =dbsTree.search(new Db(name));
		if(temp==null) out.println(ClientHandler.WELCOMEWORD+"The db name is not exsiting");
		else out.println(ClientHandler.WELCOMEWORD+"now using database: "+temp.dbName);

		return temp;
	}
	
}





//Database class
class Db implements UserDBAction, Comparable<Db>{
	static int LASTID =0;
	int dbId;
	String dbName;
	ArrayList<Table> tables = new ArrayList<Table>(); 
	MyBinaryTree<Table> tablesTree = new MyBinaryTree<Table>();
	
	
	public Db(String name){
		LASTID++;
		this.dbId = LASTID;
		this.dbName = name;
	}

	@Override
	public void addTable(String tableName, PrintWriter out, ArguSet... args) {
		Table temp = new Table(tableName,args);
		if(tablesTree.search(temp)==null){
			tablesTree.add(temp);
			out.println(ClientHandler.WELCOMEWORD+"add table successfully");
		}else{
			out.println(ClientHandler.WELCOMEWORD+"The table is already existing, please change name");
		}

	}

	@Override
	public void removeTable(String name, PrintWriter out) {

		tablesTree.remove(new Table(name), out);

	}

	@Override
	public UserTableAction useTable(String name, PrintWriter out) {
		
		Table tempTable = tablesTree.search(new Table(name));
		if (tempTable == null) out.println(ClientHandler.WELCOMEWORD+"The table name is not exsiting");
		else{
			out.println(ClientHandler.WELCOMEWORD+"Now using table: "+ name);
			
		}
		return tempTable;
	}

	@Override
	public int compareTo(Db o) {
		
		return this.dbName.compareTo(o.dbName);
	};
	
	
	
}

class Table implements UserTableAction, Comparable<Table>{
	static int LASTID =0;
	int recordID=0;
	int tableId;
	String tableName;
	Map<String, DataType> recordMeta = new LinkedHashMap<String, DataType>();
	//ArrayList<String> recordsTxt = new ArrayList<String>();
	ArrayList<MyJson> myjsons = new ArrayList<MyJson>();
	
	public Table(String name, ArguSet...arguSets){
		LASTID++;
		tableId=LASTID;
		this.tableName=name;
		for(ArguSet arguSet:arguSets){
			recordMeta.put(arguSet.name, new DataType(arguSet.type.toUpperCase(), arguSet.length));
		}		
	};
	
	@Override
	public void insertRecord(HashMap<String, String> insertMap, PrintWriter out) {
		 this.recordID++;
		 String fieldname;
		 String insertString = "{\"id\": \""+recordID+"\"";
		 try{
			for(Map.Entry<String, DataType> entry: recordMeta.entrySet()){
				fieldname = entry.getKey();
				String insertValue = insertMap.get(fieldname);
				//if(insertValue==null) continue;
				String metaType = entry.getValue().type;
				int metaLength = entry.getValue().length;
				typeCheck(metaType, insertValue);
				if(insertValue.length()>metaLength) throw new Exception();
				else{
					
					insertString += ", \""+fieldname+"\": \""+insertValue+"\"";

				}
			}
			insertString +="}";
			//recordsTxt.add(insertString);
			MyJson newJson = new MyJson(insertString);
			myjsons.add(newJson);
			out.println(ClientHandler.WELCOMEWORD+ " 1 recorded added!");
		 }catch(Exception e){
			 this.recordID--;
			 out.println(ClientHandler.WELCOMEWORD+"your input is invalid, please double check to match table structure");
		 }

	}
	public void typeCheck(String type, String insertValue){
		switch(type.toUpperCase()){
			case "INT": Integer.parseInt(insertValue);
				break;
			case "STRING":
				break;
			case "DOUBLE": Double.parseDouble(insertValue);
				break;
		}
	}
	
	//compare data with their true datatype
	/*
	public int dataCompare(String fieldName, String compare1, String compare2){
		DataType dataType =  recordMeta.get(fieldName);
		switch(dataType.type){
		
		case "STRING": return compare1.compareTo(compare2);
			
		case "INT": return Integer.parseInt(compare1) - Integer.parseInt(compare2);
		case "DOUBLE": if(Double.parseDouble(compare1) - Double.parseDouble(compare2)>0) return 1;
						else if(Double.parseDouble(compare1) - Double.parseDouble(compare2)<0) return -1;
						else return 0;
		
		default: return 0;	
		
		}
		
	}
	*/
	@Override
	public ArrayList<String> showAllRecords(PrintWriter out){
		ArrayList<String> allrecord = new ArrayList<String>();
		
		for(MyJson record: myjsons){
			allrecord.add(record.data);
			
		}
		out.println(ClientHandler.WELCOMEWORD+allrecord.size()+" records are founded");
		return allrecord;
	}
	
	
	
	
	
	public ArrayList<String> searchRecord(Condition condition, PrintWriter out) {
		ArrayList<String> afterSearch =new ArrayList<String>();
		MyJson tempJson = new MyJson("{\""+ condition.fieldName +"\": \""+ condition.condition+"\"}");
		MyJson.typeCmp = recordMeta.get(condition.fieldName).type;
		MyJson.fieldNameCmp = condition.fieldName;
		for(MyJson record: myjsons){
			switch(condition.comparer){			
			case ">": 
				if(record.compareTo(tempJson)>0) afterSearch.add(record.data);
				break;
			case "<":if(record.compareTo(tempJson)<0) afterSearch.add(record.data);
				break;
			case "=":if(record.compareTo(tempJson)==0) afterSearch.add(record.data);
				break;
			case ">=":
				if(record.compareTo(tempJson)>=0) afterSearch.add(record.data);
				break;
			case "<=":
				if(record.compareTo(tempJson)<=0) afterSearch.add(record.data);
				break;
			case "!=":
				if(record.compareTo(tempJson)!=0) afterSearch.add(record.data);
				break;
			}
		}
		
		
		/*
		JSONObject myJson;
		for(String record: recordsTxt){
			try {
				myJson = new JSONObject(record);
				switch(condition.comparer){
				
					case ">": 
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)>0) afterSearch.add(record);
						break;
					case "<":if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)<0) afterSearch.add(record);
						break;
					case "=":if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)==0) afterSearch.add(record);
						break;
					case ">=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)>=0) afterSearch.add(record);
						break;
					case "<=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)<=0) afterSearch.add(record);
						break;
					case "!=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)!=0) afterSearch.add(record);
						break;
				}
				
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/ 
		if(afterSearch.size()>0){
			out.println(ClientHandler.WELCOMEWORD+afterSearch.size()+" records are founded");
		}else out.println(afterSearch.size()+ "no record founded");
		return afterSearch;
		
	}
	
	
	
	

	@Override
	public void displayRecord(ArrayList<String> records, PrintWriter out,String...fieldNames){
		JSONObject myJson;
		String displayString;
		if(fieldNames[0].equals("*")){
			ArrayList<String> allfields = new ArrayList<String>();
			allfields.add("id");
			for(Map.Entry<String, DataType> entry: recordMeta.entrySet()){
				allfields.add(entry.getKey());
			}
			
			for(String record: records){
				try{
					myJson = new JSONObject(record);
					displayString ="{";
					for(String fieldName:allfields){
						displayString += "\""+fieldName+"\": \""+myJson.getString(fieldName)+"\",";
					}
					displayString = displayString.substring(0, displayString.length()-1)+"}";
					out.println(displayString);
					
				}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}else{
			for(String record: records){
				try{
					myJson = new JSONObject(record);
					displayString ="{";
					for(String fieldName:fieldNames){
						displayString += "\""+fieldName+"\": \""+myJson.getString(fieldName)+"\",";
					}
					displayString = displayString.substring(0, displayString.length()-1)+"}";
					out.println(displayString);
					
				}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}


	@Override
	public void updateRecord(String fieldName, String newValue, Condition condition, PrintWriter out) {
		
		MyJson tempJson = new MyJson("{\""+ condition.fieldName +"\": \""+ condition.condition+"\"}");
		MyJson.typeCmp = recordMeta.get(condition.fieldName).type;
		MyJson.fieldNameCmp = condition.fieldName;
		int count = 0;
		JSONObject myJson;
		for(MyJson record: myjsons){
			switch(condition.comparer){			
			case ">": 
				if(record.compareTo(tempJson)>0) {
					record.data = record.updateRecord(fieldName, newValue);
					count ++;
				}
				break;
			case "<":if(record.compareTo(tempJson)<0) {record.data = record.updateRecord(fieldName, newValue);
				count ++;}
				break;
			case "=":if(record.compareTo(tempJson)==0) {record.data = record.updateRecord(fieldName, newValue);
				count ++;}
				break;
			case ">=":
				if(record.compareTo(tempJson)>=0) {record.data = record.updateRecord(fieldName, newValue);
				count ++;}
				break;
			case "<=":
				if(record.compareTo(tempJson)<=0) {record.data = record.updateRecord(fieldName, newValue);
				count ++;}
				break;
			case "!=":
				if(record.compareTo(tempJson)!=0) {record.data = record.updateRecord(fieldName, newValue);
				count ++;}
				break;
			}
		}
		
		
		/*
		for(String record: recordsTxt){
			try {
				myJson = new JSONObject(record);
				switch(condition.comparer){
					case ">": if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)>0){
						myJson.put(fieldName,newValue);
						record = myJson.toString();
						count++;
					}
						break;
					case "<":if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)<0) {
						myJson.put(fieldName,newValue);
						record = myJson.toString();
						count++;
					}
						break;
					case "=":if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)==0) {
						myJson.put(fieldName,newValue);
						record = myJson.toString();
						count++;
					}
						break;
						
					case ">=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)>=0) {
							myJson.put(fieldName,newValue);
							record = myJson.toString();
							count++;
						}
						break;
					case "<=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)<=0) {
							myJson.put(fieldName,newValue);
							record = myJson.toString();
							count++;
						}
						break;
					case "!=":
						if(dataCompare(condition.fieldName, myJson.getString(condition.fieldName),condition.condition)!=0) {
							myJson.put(fieldName,newValue);
							record = myJson.toString();
							count++;
						}
						break;
						
						
						
						
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		*/
		if(count>0) out.println(ClientHandler.WELCOMEWORD+count +" record(s) are updated");
		else out.println(ClientHandler.WELCOMEWORD+"no record founded");
		
		
	}



	@Override
	public void deleteRecord(String comparer,String fieldName,	String condition, PrintWriter out) {
		
		MyJson tempJson = new MyJson("{\""+ fieldName +"\": \""+ condition+"\"}");
		MyJson.typeCmp = recordMeta.get(fieldName).type;
		MyJson.fieldNameCmp = fieldName;
		int count=0;
		for(Iterator<MyJson> iterator = myjsons.iterator(); iterator.hasNext(); ){
			MyJson record = iterator.next();
			switch(comparer){			
			case ">": 
				if(record.compareTo(tempJson)>0){ iterator.remove();
				count++;}
				break;
			case "<":if(record.compareTo(tempJson)<0){ iterator.remove();
				count++;}
				break;
			case "=":if(record.compareTo(tempJson)==0) {iterator.remove();
			count++;}
				break;
			case ">=":
				if(record.compareTo(tempJson)>=0) {iterator.remove();
				count++;}
				break;
			case "<=":
				if(record.compareTo(tempJson)<=0) {iterator.remove();
				count++;}
				break;
			case "!=":
				if(record.compareTo(tempJson)!=0) {iterator.remove();
				count++;}
				break;
			}
			if(myjsons.size()<1) break;
		}
		
		/* removed below after refactor record from string array to MyJson Object array
		JSONObject myJson;
		for(String record: recordsTxt){
			try {
				myJson = new JSONObject(record);
				switch(comparer){
					case ">": if(dataCompare(fieldName, myJson.getString(fieldName),condition)>0) recordsTxt.remove(record);
						count++;
						break;
					case "<":if(dataCompare(fieldName, myJson.getString(fieldName),condition)<0) recordsTxt.remove(record);
						count++;
						break;
					case "=":if(dataCompare(fieldName, myJson.getString(fieldName),condition)==0) recordsTxt.remove(record);
						count++;
						break;
					case ">=":
						if(dataCompare(fieldName, myJson.getString(fieldName),condition)>=0) recordsTxt.remove(record);
						count++;
						break;
					case "<=":
						if(dataCompare(fieldName, myJson.getString(fieldName),condition)<=0) recordsTxt.remove(record);
						count++;
						break;
					case "!=":
						if(dataCompare(fieldName, myJson.getString(fieldName),condition)!=0) recordsTxt.remove(record);
						count++;
						break;
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		if(count>0) out.println(ClientHandler.WELCOMEWORD+"Totally "+ count+" records are deleted");
		else out.println(ClientHandler.WELCOMEWORD+ "0 record is founded");
	}
	
	

	class DataType{
		String type;
		int length;
		public DataType(String type, int length){
			this.type=type;
			this.length=length;
		}
	}



	@Override
	public int compareTo(Table o) {
		return this.tableName.compareTo(o.tableName);
	}







	
	
	
}