package org.objectpocket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.objectpocket.ObjectPocketSimpleTest.OuterClass.InnerClass;

/**
 * 
 * 
 * 
 * @author Edmund Klaus
 *
 */
public class ObjectPocketSimpleTest extends FileStoreTest {

    @Test
    public void testAddNull() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	objectPocket.add(null);
	objectPocket.store();
	objectPocket.load();
	Collection<Object> findAll = objectPocket.findAll(Object.class);
	assertNull(findAll);
	objectPocket.add(null, "abc");
	objectPocket.store();
	objectPocket.load();
	findAll = objectPocket.findAll(Object.class);
	assertNull(findAll);
	objectPocket.add(null, null);
	objectPocket.store();
	objectPocket.load();
	findAll = objectPocket.findAll(Object.class);
	assertNull(findAll);
	objectPocket.add(new BeanWithCustomConstructor("abc"), null);
	objectPocket.store();
	objectPocket.load();
	Collection<BeanWithCustomConstructor> strings = objectPocket
		.findAll(BeanWithCustomConstructor.class);
	assertTrue(strings.size() == 1);
    }

    @Test
    public void testFindNull() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	objectPocket.add(new BeanWithCustomConstructor("abc"));
	objectPocket.store();
	objectPocket.load();
	BeanWithCustomConstructor find = objectPocket.find(null, null);
	assertNull(find);
	find = objectPocket.find(null, BeanWithCustomConstructor.class);
	assertNull(find);
	find = objectPocket.find(" ", BeanWithCustomConstructor.class);
	assertNull(find);
	find = objectPocket.find("ziwzd7", null);
	assertNull(find);
	find = objectPocket.find(" ", null);
	assertNull(find);
    }

    @Test
    public void testRemoveNull() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	try {
	    objectPocket.remove(null);
	} catch (Throwable t) {
	    assertTrue(false);
	}
    }

    @Test
    public void testFindAllNull() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	objectPocket.add(new BeanWithCustomConstructor("abc"));
	objectPocket.store();
	objectPocket.load();
	Collection<BeanWithCustomConstructor> findAll = objectPocket
		.findAll(null);
	assertNull(findAll);
    }

    @Test
    public void testSimpleBean() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
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
	ObjectPocket objectPocket = getObjectPocket();
	BeanWithCustomConstructor bean = new BeanWithCustomConstructor(
		"beanName");
	objectPocket.add(bean);
	objectPocket.store();
	objectPocket.load();
	Collection<BeanWithCustomConstructor> beans = objectPocket
		.findAll(BeanWithCustomConstructor.class);
	assertTrue(beans.size() == 1);
	BeanWithCustomConstructor found = beans.iterator().next();
	assertFalse(found.equals(bean));
	assertTrue(found.getName().equals(bean.getName()));
    }

    @Test
    public void testBeanWithNoGettersAndSetters() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	BeanWithNoGettersAndSetters bean = new BeanWithNoGettersAndSetters();
	bean.name = "beanName";
	objectPocket.add(bean);
	objectPocket.store();
	objectPocket.load();
	Collection<BeanWithNoGettersAndSetters> beans = objectPocket
		.findAll(BeanWithNoGettersAndSetters.class);
	assertTrue(beans.size() == 1);
	BeanWithNoGettersAndSetters found = beans.iterator().next();
	assertFalse(found.equals(bean));
	assertFalse(found.equals(bean));
	assertTrue(found.name.equals(bean.name));
    }

    @Test
    public void testBeanWithReferenceToObject() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
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
	assertTrue(found.getAddress().getCity()
		.equals(person.getAddress().getCity()));
    }

    @Test
    public void testNullConstructor() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
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
	ObjectPocket objectPocket = getObjectPocket();
	OuterClass outerClass = new OuterClass();
	outerClass.setId(UUID.randomUUID().toString());
	InnerClass referenceToInnerClass = outerClass
		.getReferenceToInnerClass();
	objectPocket.add(outerClass);
	objectPocket.store();
	objectPocket.load();
	Collection<OuterClass> outerClasses = objectPocket
		.findAll(OuterClass.class);
	assertTrue(outerClasses.size() == 1);
	OuterClass found = outerClasses.iterator().next();
	assertTrue(outerClass.getId().equals(found.getId()));
	assertTrue(referenceToInnerClass.getId().equals(
		found.getReferenceToInnerClass().getId()));
    }

    @Test
    public void testRemove() throws Exception {
	ObjectPocket objectPocket = getObjectPocket();
	SimpleBean simpleBean = new SimpleBean();
	simpleBean.setName("simple bean");
	
	objectPocket.add(simpleBean);
	assertTrue(objectPocket.findAll(SimpleBean.class).size() == 1);
	objectPocket.remove(simpleBean);
	assertNull(objectPocket.findAll(SimpleBean.class));
	
	objectPocket.add(simpleBean);
	objectPocket.store();
	objectPocket.load();
	
	Collection<SimpleBean> findAll = objectPocket.findAll(SimpleBean.class);
	assertTrue(findAll.size() == 1);
	SimpleBean find = findAll.iterator().next();
	assertTrue(find.getName().equals(simpleBean.getName()));
	objectPocket.remove(find);
	assertNull(objectPocket.findAll(SimpleBean.class));
	objectPocket.store();
	assertNull(objectPocket.findAll(SimpleBean.class));
	objectPocket.load();
	assertNull(objectPocket.findAll(SimpleBean.class));
    }
    
    @Test
    /**
     * This ensures that the custom filename is beeing preserved.
     * 
     * @throws Exception
     */
    public void rewriteToCustomFilename() throws Exception {
        String customFilename = "customfilename";
        String customFilenameWithExtension = customFilename + ".json";
        ObjectPocket objectPocket = getObjectPocket();
        SimpleBean simpleBean = new SimpleBean();
        simpleBean.setName("simple bean");
        objectPocket.add(simpleBean, customFilename);
        objectPocket.store();
        File f = new File(FILESTORE);
        String[] list = f.list();
        boolean foundFile = false;
        for (String string : list) {
            if (string.equals(customFilenameWithExtension)) {
                foundFile = true;
            }
        }
        assertTrue(foundFile);
        objectPocket = getObjectPocket();
        objectPocket.load();
        objectPocket.store();
        list = f.list();
        foundFile = false;
        for (String string : list) {
            if (string.equals(customFilenameWithExtension)) {
                foundFile = true;
            }
        }
        assertTrue(foundFile);
    }
    
    @Test
    public void testBackup() throws Exception {
        
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

}
