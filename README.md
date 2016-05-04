# ObjectPocket

## What is it?
ObjectPocket is a simple store/load library for plain Java objects. It serializes Java objects to JSON. The serialized objects are still human readable and modifiable by hand or other tools.

## What to use it for?
Generally you can use it for every kind of object store/load task. Some very handy use cases are listed below.

### Use Case: Configuration
Use ObjectPocket to handle your configuration. You will never have to use `Integer.parseInt()` and `Boolean.parseBoolean()` again, to load a configuration!
```
class Configuration {
    private String host;
    private int port;
    private boolean autoconnect = true;
}
```
The resulting file will look like:
```
{"objects":[
{
  "op_type": "org.package.Configuration",
  "op_id": "cdd4b552-4484-471a-a52f-6a66ee28e6d4",
  "host": "localhost",
  "port": 12345,
  "autoconnect": true
}
]}
```
You can even edit the result by hand and load it into your application. It's plain JSON.

### Use Case: User Management
Use ObjectPocket to easily handle your user data. ObjectPocket loads and stores all of its data at once. There is no database like access to the files on disk. Nevertheless it's very fast in doing that. A dataset of around 100.000 objects needs around 1 second to load and 1 second to store on a standard developer machine. Most of the time you won't have to worry about the performance.
```
class User {
    private String name = "username";
    private Date birthdate = new Date();
    private Address address;
    public void setAddress(Address address) {
		this.address = address;
	}
}
class Address {
	private String country;
	private int postalCode;
	public void setCountry(String country) {
		this.country = country;
	}
	public void setPostalCode(int postalCode) {
		this.postalCode = postalCode;
	}
}
```
The resulting file will look like:
```
{"objects":[
{
  "op_type": "org.package.User",
  "op_id": "2c8c5270-caac-441c-94d6-3942c8a182af",
  "name": "username",
  "birthdate": "Mar 3, 2016 3:09:27 PM",
  "address": {
    "country": "Thailand",
    "postalCode": 123456
  }
]}
```

### Use Case: Window State
Did you always wanted to save the last state of your application's window when closing the application? And the position and size of the window. To restore the state when starting the application again?
```
class WindowState {
	private Point position;
	private Dimension dimension;
	private int State;
}

WindowState windowState = new WindowState();
// fill windowState with values and store
...
ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("config");
objectPocket.add(windowState);
objectPocket.store();

// load last window state
objectPocket.load();
WindowState windowState = objectPocket.findAll(WindowState.class).iterator().next();
```


## How to use it
You can store/load just every Java object you want. There are no prerequisites like constructors, getters/setters, Annotations, Interfaces, ... ObjectPocket also support inheritance. It will store the superclass fields for you if you extend a class. 

### Store Objects
```
MyClass obj = new MyClass();
ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("directory");
objectPocket.add(obj);
objectPocket.store();
```

Result:

* ObjectPocket creates a new file "org.package.MyClass" in "directory". This is where the MyClass objects will go.
* It also creates a file named ".op_index".
* If "directory" does not exist, ObjectPocket automatically creates it 

### Load Objects
```
ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("directory");
objectPocket.load();
MyClass obj = objectPocket.findAll(MyClass.class).iterator().next();
```

### Store Objects in specific files
You can pass a filename along with an object to store that object in the given file.
```
...
objectPocket.add(obj, "filename");
objectPocket.store();
```
Result:
 
* ObjectPocket creates a new file "filename" in "directory". This is where the MyClass objects will go now, instead of "org.package.MyClass"
* When loading the data, ObjectPocket will automatically detect in which files the different object types have been stored. This is where ".op_index" helps.

## Download
The latest build is availabe at The Central Repository.

### Maven
```
<dependency>
    <groupId>org.objectpocket</groupId>
    <artifactId>objectpocket</artifactId>
    <version>0.0.4</version>
</dependency>
```

### Direct Download
http://repo1.maven.org/maven2/org/objectpocket/objectpocket/0.0.3/

## Version
Experimental stage. 
current version: 0.0.4

## Tech
ObjectPocket uses the Gson library to convert Java objects to JSON. https://github.com/google/gson

## License
ObjectPocket is released under the [Apache 2.0 license](LICENSE).
