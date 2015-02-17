package com.ibm.personafusion.controller;

import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.ibm.personafusion.Config;
import com.ibm.personafusion.Constants;
import com.ibm.personafusion.model.Person;

/** Handles the GET /api/people endpoint.
 *  Returns a JSON representation of all of the Person objects. 
 *  @author Sean Welleck **/
@Path("/test")
public class TestService 
{
	
	@GET
	public Response handle()
	{
		Random rn = new Random();
		
		int i = rn.nextInt(10000);
		
		return Response.ok("TestName"+Integer.toString(i)).build();
	}
}