import java.util.ArrayList;
import java.util.List;

import com.ibm.personafusion.db.CloudantClient;
import com.ibm.personafusion.model.Person;
import com.ibm.personafusion.model.Trait;


public class CloudantConsumer {
	
	public static void main(String[] args) throws Exception
	{
		CloudantClient cc = new CloudantClient();
		List<Trait> traits = new ArrayList<Trait>();
		traits.add(new Trait("programming", .9));
		traits.add(new Trait("being awesome", .95));
		Person p2 = new Person("Bob", traits, null);
		p2.group = "group2";
		cc.putPerson(p2);
		
		Person testPerson = cc.getPerson("Bob");
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
