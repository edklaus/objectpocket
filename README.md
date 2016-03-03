# ObjectPocket
ObjectPocket is a simple store/load library for plain Java objects. It serializes Java objects to JSON. The serialized objects are still human readable and modifiable by hand or other tools.

### Usage
You can store/load just every Java object you want. There are no prerequisites like constructors, getters/setters, Annotations, Interfaces, ... ObjectPocket also support inheritance. It will store the superclass fields for you if you extend a class. 

#### Store
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

#### Load
```
ObjectPocket objectPocket = new ObjectPocketBuilder().createFileObjectPocket("directory");
objectPocket.load();
MyClass obj = objectPocket.findAll(MyClass.class).iterator().next();
```

#### Storing objects in specific files
You can pass a filename along with an object to store that object in the given file.
```
...
objectPocket.add(obj, "filename");
objectPocket.store();
```
Result:
 
* ObjectPocket creates a new file "filename" in "directory". This is where the MyClass objects will go now, instead of "org.package.MyClass"
* When loading the data, ObjectPocket will automatically detect in which files the different object types have been stored. This is where ".op_index" helps.


### Version
Experimental stage. 
current version: 0.0.2

### Tech
ObjectPocket uses the Gson library to convert Java objects to JSON. https://github.com/google/gson

### License
ObjectPocket is released under the [Apache 2.0 license](LICENSE).
