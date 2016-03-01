package org.objectpocket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.objectpocket.ObjectPocketTestSimple.OuterClass.InnerClass;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketTestSimple {
	
	@Test
	public void testSimpleBean() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		SimpleBean bean = new SimpleBean();
		bean.setName("beanName");
		objectPocket.add(bean);
		objectPocket.store();
		objectPocket.load();
		Collection<SimpleBean> beans = objectPocket.findAll(SimpleBean.class);
		assertTrue(beans.size() == 1);
		SimpleBean found = beans.iterator().next();
		assertFalse(found.equals(bean));
		assertTrue(found.getName().equals(bean.getName()));
	}
	
	@Test
	public void testBeanWithCustomConstrucor() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		BeanWithCustomConstructor bean = new BeanWithCustomConstructor("beanName");
//		japer.add(bean);
//		japer.persist();
//		japer.load();
//		BeanWithCustomConstructor find = japer.find(bean.getId(), BeanWithCustomConstructor.class);
//		assertFalse(find.equals(bean));
//		assertTrue(find.getName().equals(bean.getName()));
	}
	
	@Test
	public void testBeanWithNoGettersAndSetters() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		BeanWithNoGettersAndSetters bean = new BeanWithNoGettersAndSetters();
//		bean.name = "beanName";
//		japer.add(bean);
//		japer.persist();
//		japer.load();
//		BeanWithNoGettersAndSetters find = japer.find(bean.getId(), BeanWithNoGettersAndSetters.class);
//		assertFalse(find.equals(bean));
//		assertTrue(bean.getId().equals(find.getId()));
//		assertTrue(bean.name.equals(find.name));
	}
	
	@Test
	public void testBeanWithReferenceToIdentifiable() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		Address address = new Address();
//		address.setCity("Karlsruhe");
//		Person person = new Person("person1", address);
//		japer.add(person);
//		japer.persist();
//		japer.load();
//		Person find = japer.find(person.getId(), Person.class);
//		assertFalse(find.equals(person));
//		assertTrue(find.getName().equals(person.getName()));
//		assertTrue(find.getAddress().getCity().equals(person.getAddress().getCity()));
	}
	
	@Test
	public void testNullConstructor() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		Person person = new Person(null, null);
//		japer.add(person);
//		japer.persist();
//		japer.load();
//		Person find = japer.find(person.getId(), Person.class);
//		assertFalse(find.equals(person));
//		assertTrue(find.getName() == null);
//		assertTrue(find.getAddress() == null);
	}
	
	@Test
	public void testCyclicReferences() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		Address address = new Address();
//		address.setCity("Karlsruhe");
//		Person person = new Person("person1", address);
//		address.setInhabitant(person);
//		japer.add(person);
//		japer.persist();
//		japer.load();
//		Collection<Person> persons = japer.findAll(Person.class);
//		Collection<Address> addresses = japer.findAll(Address.class);
//		assertTrue(persons.size() == 1);
//		assertTrue(addresses.size() == 1);
//		Person foundPerson = persons.iterator().next();
//		Address foundAddress = addresses.iterator().next();
//		assertTrue(foundAddress.getInhabitant().equals(foundPerson));
//		assertTrue(foundPerson.getAddress().equals(foundAddress));
	}
	
	@Test
	public void testInnerClass() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		OuterClass outerClass = new OuterClass();
//		InnerClass referenceToInnerClass = outerClass.getReferenceToInnerClass();
//		japer.add(outerClass);
//		japer.persist();
//		japer.load();
//		OuterClass find = japer.find(outerClass.getId(), OuterClass.class);
//		assertTrue(outerClass.getId().equals(find.getId()));
//		assertTrue(referenceToInnerClass.getId().equals(find.getReferenceToInnerClass().getId()));
	}
	
	@Test
	public void testRemove() throws Exception {
//		Japer japer = new JaperFactory().createMemoryJaper();
//		SimpleBean simpleBean = new SimpleBean();
//		simpleBean.setName("simple bean");
//		japer.add(simpleBean);
//		japer.persist();
//		japer.load();
//		Collection<SimpleBean> findAll = japer.findAll(SimpleBean.class);
//		assertTrue(findAll.size() == 1);
//		SimpleBean find = findAll.iterator().next();
//		assertTrue(find.getId().equals(simpleBean.getId()));
//		japer.remove(simpleBean);
//		assertTrue(japer.findAll(SimpleBean.class).size() == 0);
//		japer.persist();
//		assertTrue(japer.findAll(SimpleBean.class).size() == 0);
//		japer.load();
//		assertTrue(japer.findAll(SimpleBean.class).size() == 0);
	}
	
	public class SimpleBean {
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public class BeanWithCustomConstructor {
		private String name;
		public BeanWithCustomConstructor(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public class BeanWithNoGettersAndSetters {
		public String name;
	}
	
	public class Person {
		private String name;
		private Address address;
		public Person(String name, Address address) {
			this.name = name;
			this.address = address;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Address getAddress() {
			return address;
		}
		public void setAddress(Address address) {
			this.address = address;
		}
	}
	
	public class Address {
		private String city;
		private Person inhabitant;
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public Person getInhabitant() {
			return inhabitant;
		}
		public void setInhabitant(Person inhabitant) {
			this.inhabitant = inhabitant;
		}
	}
	
	public class OuterClass {
		private InnerClass referenceToInnerClass;
		public OuterClass() {
			referenceToInnerClass = new InnerClass();
		}
		public InnerClass getReferenceToInnerClass() {
			return referenceToInnerClass;
		}
		public void setReferenceToInnerClass(InnerClass referenceToInnerClass) {
			this.referenceToInnerClass = referenceToInnerClass;
		}
		public class InnerClass {
			
		}
	}

}
