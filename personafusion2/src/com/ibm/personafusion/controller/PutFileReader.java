package com.ibm.personafusion.controller;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.ibm.personafusion.Config;
import com.ibm.personafusion.Constants;
import com.ibm.personafusion.db.CloudantClient;
import com.ibm.personafusion.model.Person;

/** Handles the GET /api/people endpoint.
 *  Returns a JSON representation of all of the Person objects. 
 *  @author Sean Welleck **/
@Path("/putfileread")
public class PutFileReader 
{
	
	CloudantClient cc = new CloudantClient();
	
	String r = cc.putFileReader();
	
	@GET
	public Response handle()
	{
		
		return Response.ok(r).build();
		
	}
}