package com.ibm.personafusion.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.personafusion.Config;
import com.ibm.personafusion.Constants;
import com.ibm.personafusion.controller.JsonUtils;
import com.ibm.personafusion.model.Person;
import com.ibm.personafusion.model.Trait;

/** A class for communicating with the Cloudant datastore. 
 *  See main() for example usage.
 *  
 *  @author Sean Welleck**/
public class CloudantClient 
{
	private HttpClient httpClient;
	private CouchDbConnector dbc;
	
	private int port;
	private String name;
	private String host;
	private String username;
	private String password;
	
	private JSONArray cloudant;
	private JSONObject cloudantInstance;
	private JSONObject cloudantCredentials;
	
	public CloudantClient()
	{
		this.httpClient = null;
		 
		 try {
           		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			JSONObject vcap;
			
			if (VCAP_SERVICES != null) {
				vcap = (JSONObject) JSONObject.parse(VCAP_SERVICES);
				cloudant = (JSONArray) vcap.get("cloudantNoSQLDB");
				cloudantInstance = (JSONObject) cloudant.get(0);
				cloudantCredentials = (JSONObject) cloudantInstance.get("credentials");
			}
		 } catch (IOException e) {
	            e.printStackTrace();
		 }
		 
		this.port = Config.CLOUDANT_PORT;
		this.host = "81f4f4aa-e541-42c3-a1d1-dca10c2f6f96-bluemix.cloudant.com";
		this.username = "81f4f4aa-e541-42c3-a1d1-dca10c2f6f96-bluemix";
		this.password = "05717d422d563b2a759ca7cfd56cc28971123a0f1f846cae49e15db3584c7f1e";
		this.name = Config.CLOUDANT_NAME;
		this.dbc = this.createDBConnector();
	}
	
	/** Put a Person into Cloudant using person.name as the unique id.
	 *  Stored as :
	 *  { 
	 *  	id: person.name, 
	 *  	type: Person.class, 
	 *  	group: person.group, 
	 *  	json: toJSON(person) 
	 *  }
	 */
	public void putPerson(Person p)
	{
		HashMap<String, Object> data = new HashMap<String, Object>();
		String name = p.name.toUpperCase();
		String group = p.group;
		data.put(Constants.ID_KEY, name);
		data.put(Constants.TYPE_KEY, Person.class.getName());
		data.put(Constants.GROUP_KEY, group);
		System.out.println(data.get(Constants.TYPE_KEY));
		data.put(Constants.JSON_KEY, JsonUtils.getJson(p));
		this.putItem(data);
	}
	
	/** Get a Person from Cloudant using name as the unique id. **/
	public Person getPerson(String name)
	{
		name = name.toUpperCase();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> obj = this.dbc.get(HashMap.class, name);
		Person p = JsonUtils.getPersonFromJson((String)obj.get(Constants.JSON_KEY));
		return p;
	}
	
	/** Get all Person objects from Cloudant. **/
	public List<Person> getAllPeople()
	{
		List<Person> people = new ArrayList<Person>();
		List<String> docIds = dbc.getAllDocIds();
		for(String docId : docIds)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, Object> obj = this.dbc.get(HashMap.class, docId);
			if (obj.get(Constants.TYPE_KEY) != null && 
				obj.get(Constants.TYPE_KEY).equals(Person.class.getName()))
			{
				String json = (String)obj.get(Constants.JSON_KEY);
				Person p = JsonUtils.getPersonFromJson(json);
				people.add(p);
			}
		}
		System.out.println(
			String.format("Retrieved %d Person entries.", people.size()));
		return people;
	}
	
	/** Get all Person objects in the specified group from Cloudant. **/
	public List<Person> getAllPeopleInGroup(String groupName)
	{
		putCS560Test();
		
		List<Person> people = new ArrayList<Person>();
		List<String> docIds = dbc.getAllDocIds();
		for(String docId : docIds)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, Object> obj = this.dbc.get(HashMap.class, docId);
			if (obj.get(Constants.TYPE_KEY) != null && 
				obj.get(Constants.TYPE_KEY).equals(Person.class.getName()) &&
				obj.get(Constants.GROUP_KEY) != null &&
				obj.get(Constants.GROUP_KEY).equals(groupName))
			{
				String json = (String)obj.get(Constants.JSON_KEY);
				Person p = JsonUtils.getPersonFromJson(json);
				people.add(p);
			}
		}
		System.out.println(String.format(
					"Retrieved %d Person entries for group %s.", 
					people.size(), groupName));
		return people;
	}
	
	/** Get all Person objects in the specified group from Cloudant. **/
	public List<Person> getAllPeopleNotInGroup(String groupName)
	{
		List<Person> people = new ArrayList<Person>();
		List<String> docIds = dbc.getAllDocIds();
		for(String docId : docIds)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, Object> obj = this.dbc.get(HashMap.class, docId);
			if (obj.get(Constants.TYPE_KEY) != null && 
				obj.get(Constants.TYPE_KEY).equals(Person.class.getName()) &&
				obj.get(Constants.GROUP_KEY) != null &&
				!obj.get(Constants.GROUP_KEY).equals(groupName))
			{
				String json = (String)obj.get(Constants.JSON_KEY);
				Person p = JsonUtils.getPersonFromJson(json);
				people.add(p);
			}
		}
		System.out.println(String.format(
					"Retrieved %d Person entries that are not in group %s.", 
					people.size(), groupName));
		return people;
	}
	
	/** Delete all documents from the Cloudant datastore. Use with caution. **/
	public void deleteAll()
	{
		List<String> docIds = this.dbc.getAllDocIds();
		int startSize = docIds.size();
		for(String docId : docIds)
		{
			@SuppressWarnings("unchecked")
			HashMap<String, Object> obj = this.dbc.get(HashMap.class, docId);
			this.dbc.delete(obj);
		}
		docIds = this.dbc.getAllDocIds();
		int endSize = docIds.size();
		System.out.println(
				String.format(
					"Deleted all entries. Starting size: %d. Current size: %d.",
					startSize, endSize));
	}
	
	public void deletePerson(Person p)
	{
		String name = p.name.toUpperCase();
		List<String> docIds = this.dbc.getAllDocIds();
		int startSize = docIds.size();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> obj = this.dbc.get(HashMap.class, name);
		this.dbc.delete(obj);
		docIds = this.dbc.getAllDocIds();
		int endSize = docIds.size();
		System.out.println(
				String.format(
					"Deleted entry %s. Starting size: %d. Current size: %d.",
					name, startSize, endSize));
	}
	
	/** Put a generic item modeled as Key-Value pairs into Cloudant. **/
	private void putItem(HashMap<String, Object> data)
	{
		if (data == null) 
		{ 
			System.err.println("data cannot be null in putItem()"); 
			return;
		}
		String id = (String)data.get(Constants.ID_KEY);
		if (id == null)   
		{ 
			System.err.println("data must have an _id field."); 
			return;
		}
		if (this.dbc.contains(id)) 
		{ 
			System.err.println("Didn't putItem. _id=" + id + " already exists."); 
			return;
		}
		this.dbc.create(data);
		System.out.println("Put _id=" + id + " into the datastore."); 
	}
	
	private CouchDbConnector createDBConnector() 
	{
		CouchDbInstance dbInstance = null;
		
		System.out.println("Creating CouchDB instance...");
		System.out.println(this.username);
		this.httpClient = new StdHttpClient.Builder()
		.host(this.host)
		.port(this.port)
		.username(this.username)
		.password(this.password)
		.enableSSL(true)
		.relaxedSSLSettings(true)
		.build();

		dbInstance = new StdCouchDbInstance(this.httpClient);
		CouchDbConnector dbc = new StdCouchDbConnector(this.name, dbInstance);
		dbc.createDatabaseIfNotExists();
		return dbc;
	}
	
	private void closeDBConnector()
	{
		if (httpClient != null)
		{
			httpClient.shutdown();
		}
	}
	
	public String putCS560Test()
	{
		String response="success call service";
		
		
		String name2="";
		
		 try {
	            URL url = new URL("http://talent-manager-awesome2.mybluemix.net/api/test");
	            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	            String inputLine; 
	 
	            while ((inputLine = in.readLine()) != null) {
	                // Process each line.
	               // System.out.println(inputLine);
	            	name2 = inputLine;
	            }
	            in.close(); 
	            
	            //response = inputLine;
	 
	        } catch (MalformedURLException me) {
	            System.out.println(me); 
	            response = me.toString();
	        } catch (IOException ioe) {
	            System.out.println(ioe);
	            response=ioe.toString();
	        }
		
		// response="success";
		 
	
		CloudantClient cc = new CloudantClient();

		Person p = new Person(name2, null, null);
		p.group = "group1";
		cc.putPerson(p);
		
		return response;
		
		
	}
	
	
	public String putFileReader()
	{
		CloudantClient cc = new CloudantClient();
		String response = "success read";
		
				List<String> nameList = new ArrayList<String>();
				
				BufferedReader br = null;
				 
				try {
		 
					String sCurrentLine;
		 
					br = new BufferedReader(new FileReader("File/NameFile"));
		 
					while ((sCurrentLine = br.readLine()) != null) {
						//System.out.println(sCurrentLine);
						if(!nameList.contains(sCurrentLine))
						{
							nameList.add(sCurrentLine);
						}
						
					}
		 
				} catch (IOException e) {
					e.printStackTrace();
					response = e.toString();
				} finally {
					try {
						if (br != null)br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
						response = ex.toString();
					}
				}
				
				for(int i=0;i<nameList.size();i++){
				Person p = new Person(nameList.get(i), null, null);
				p.group = "group1";
				cc.putPerson(p);
				}
				
				return response;
	}
	
	
	/** Example usage. **/
	public static void main(String[] args) throws Exception
	{
		CloudantClient cc = new CloudantClient();
		
		Person p = new Person("Pradyumna Doddala", null, null);
		p.group = "group1";
		cc.putPerson(p);
		
		List<Trait> traits = new ArrayList<Trait>();
		traits.add(new Trait("programming", .6));
		traits.add(new Trait("being awesome", .99));
		Person p2 = new Person("Sai Bharath Attaluri", traits, null);
		p2.group = "group2";
		cc.putPerson(p2);
		
		Person testPerson = cc.getPerson("Sai Bharath Attaluri");
		System.out.println(testPerson.name);
		System.out.println(testPerson.traits);
		
		List<Person> people = cc.getAllPeople();
		System.out.println("There are " + people.size() + " people.");
		
		List<Person> g1ppl = cc.getAllPeopleInGroup("group1");
		System.out.println("There are " + g1ppl.size() + " people in group1.");
		
		List<Person> notg1ppl = cc.getAllPeopleNotInGroup("group1");
		System.out.println(notg1ppl.get(0).name + " is not in group1.");
		
		/*cc.deletePerson(p);
		cc.deletePerson(p2);*/
		
		cc.closeDBConnector();
	}
}
