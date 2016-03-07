package org.objectpocket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.objectpocket.ObjectPocketTestSimple.OuterClass.InnerClass;
import org.objectpocket.annotations.Entity;

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
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		BeanWithCustomConstructor bean = new BeanWithCustomConstructor("beanName");
		objectPocket.add(bean);
		objectPocket.store();
		objectPocket.load();
		Collection<BeanWithCustomConstructor> beans = objectPocket.findAll(BeanWithCustomConstructor.class);
		assertTrue(beans.size() == 1);
		BeanWithCustomConstructor found = beans.iterator().next();
		assertFalse(found.equals(bean));
		assertTrue(found.getName().equals(bean.getName()));
	}
	
	@Test
	public void testBeanWithNoGettersAndSetters() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		BeanWithNoGettersAndSetters bean = new BeanWithNoGettersAndSetters();
		bean.name = "beanName";
		objectPocket.add(bean);
		objectPocket.store();
		objectPocket.load();
		Collection<BeanWithNoGettersAndSetters> beans = objectPocket.findAll(BeanWithNoGettersAndSetters.class);
		assertTrue(beans.size() == 1);
		BeanWithNoGettersAndSetters found = beans.iterator().next();
		assertFalse(found.equals(bean));
		assertFalse(found.equals(bean));
		assertTrue(found.name.equals(bean.name));
	}
	
	@Test
	public void testBeanWithReferenceToObject() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		Address address = new Address();
		address.setCity("Karlsruhe");
		Person person = new Person("person1", address);
		objectPocket.add(person);
		objectPocket.store();
		objectPocket.load();
		Collection<Person> persons = objectPocket.findAll(Person.class);
		assertTrue(persons.size() == 1);
		Person found = persons.iterator().next();
		assertFalse(found.equals(person));
		assertTrue(found.getName().equals(person.getName()));
		assertTrue(found.getAddress().getCity().equals(person.getAddress().getCity()));
	}
	
	@Test
	public void testNullConstructor() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		Person person = new Person(null, null);
		objectPocket.add(person);
		objectPocket.store();
		objectPocket.load();
		Collection<Person> persons = objectPocket.findAll(Person.class);
		assertTrue(persons.size() == 1);
		Person found = persons.iterator().next();
		assertFalse(found.equals(person));
		assertTrue(found.getName() == null);
		assertTrue(found.getAddress() == null);
	}
	
	@Test
	public void testInnerClass() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		OuterClass outerClass = new OuterClass();
		outerClass.setId(UUID.randomUUID().toString());
		InnerClass referenceToInnerClass = outerClass.getReferenceToInnerClass();
		objectPocket.add(outerClass);
		objectPocket.store();
		objectPocket.load();
		Collection<OuterClass> outerClasses = objectPocket.findAll(OuterClass.class);
		assertTrue(outerClasses.size() == 1);
		OuterClass found = outerClasses.iterator().next();
		assertTrue(outerClass.getId().equals(found.getId()));
		assertTrue(referenceToInnerClass.getId().equals(found.getReferenceToInnerClass().getId()));
	}
	
	@Test
	public void testRemove() throws Exception {
//		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
//		SimpleBean simpleBean = new SimpleBean();
//		simpleBean.setName("simple bean");
//		objectPocket.add(simpleBean);
//		objectPocket.store();
//		objectPocket.load();
//		Collection<SimpleBean> findAll = objectPocket.findAll(SimpleBean.class);
//		assertTrue(findAll.size() == 1);
//		SimpleBean find = findAll.iterator().next();
//		assertTrue(find.getId().equals(simpleBean.getId()));
//		objectPocket.remove(simpleBean);
//		assertTrue(objectPocket.findAll(SimpleBean.class).size() == 0);
//		objectPocket.store();
//		assertTrue(objectPocket.findAll(SimpleBean.class).size() == 0);
//		objectPocket.load();
//		assertTrue(objectPocket.findAll(SimpleBean.class).size() == 0);
	}
	
	@Test
	public void testCyclicReferences() throws Exception {
		ObjectPocket objectPocket = new ObjectPocketBuilder().createMemoryObjectPocket();
		Address2 address = new Address2();
		address.setCity("Karlsruhe");
		Person2 person = new Person2("person1", address);
		address.setInhabitant(person);
		objectPocket.add(person);
		objectPocket.store();
		objectPocket.load();
		Collection<Person2> persons = objectPocket.findAll(Person2.class);
		Collection<Address2> addresses = objectPocket.findAll(Address2.class);
		assertTrue(persons.size() == 1);
		assertTrue(addresses.size() == 1);
		Person2 foundPerson = persons.iterator().next();
		Address2 foundAddress = addresses.iterator().next();
		assertFalse(foundAddress.getInhabitant() == null);
		assertFalse(foundPerson.getAddress() == null);
		assertTrue(foundAddress.getInhabitant().equals(foundPerson));
		assertTrue(foundPerson.getAddress().equals(foundAddress));
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
		private String id;
		private InnerClass referenceToInnerClass;
		public OuterClass() {
			referenceToInnerClass = new InnerClass();
			referenceToInnerClass.setId(UUID.randomUUID().toString());
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public InnerClass getReferenceToInnerClass() {
			return referenceToInnerClass;
		}
		public void setReferenceToInnerClass(InnerClass referenceToInnerClass) {
			this.referenceToInnerClass = referenceToInnerClass;
		}
		public class InnerClass {
			private String id;
			public String getId() {
				return id;
			}
			public void setId(String id) {
				this.id = id;
			}
		}
	}
	
	@Entity
	public class Person2 {
		private String name;
		private Address2 address;
		public Person2() {
		}
		public Person2(String name, Address2 address) {
			this.name = name;
			this.address = address;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Address2 getAddress() {
			return address;
		}
		public void setAddress(Address2 address) {
			this.address = address;
		}
	}
	
	@Entity
	public class Address2 {
		private String city;
		private Person2 inhabitant;
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public Person2 getInhabitant() {
			return inhabitant;
		}
		public void setInhabitant(Person2 inhabitant) {
			this.inhabitant = inhabitant;
		}
	}

}
