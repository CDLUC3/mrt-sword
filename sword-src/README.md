SWORD 2.0 Server
================

This server library is an implementation of the SWORD 2.0 standard defined here

http://swordapp.org/sword-v2/sword-v2-specifications/

Use Maven Central Version
-------------------------

Include this dependency into your pom.xml to obtain the 1.0 release version of this software

    <dependency>
        <groupId>org.swordapp</groupId>
        <artifactId>sword2-server</artifactId>
        <version>1.0</version>
        <type>jar</type>
        <classifier>classes</classifier>
    </dependency>
    <dependency>
        <groupId>org.swordapp</groupId>
        <artifactId>sword2-server</artifactId>
        <version>1.0</version>
        <type>war</type>
    </dependency>


Build
-----

To build this library use maven 2:

    mvn clean package

In the root directory will build the software

Usage
-----

This library provides a layer between the implementer's software and the web API provided by SWORDv2.  There are two distinct tasks for the implementer when using this library:

## Implementing functionality through interfaces

The library converts web API requests into objects, and passes them to implementations of interfaces which it provides.  The interfaces that must be implemented are:

    CollectionDepositManager
    CollectionListManager
    ContainerManager
    MediaResourceManager
    ServiceDocumentManager
    StatementManager

* CollectionDepositManager - this contains the method signatures required to create a new item on the server
* CollectionListMnager - this contains the method signatures required to list the contents of a collection
* ContainerManager - this contains the method signatures for retrieving and updating an existing object on the server
* MediaResourceManager - this contains the method signatures for retrieving and updating the content within a Container on the server
* ServiceDocumentManager - this contains the method signatures for retrieving service documents from the server
* StatementManager - this contains the method signatures for retrieving Statement objects from the server

In addition, the library takes its configuration from the interface:

    SwordConfiguration

If no implementation of the configuration is provided, implementers can use the default, which provides a basic set of config options:

    SwordConfigurationDefault

## Binding the server library's API to your servlet container

In order to have the server respond to web requests, and call the appropriate implementations, it is necessary to either use the standard servlets which come bundled with the library, or to write your own.

Most of the work of the library is done in classes which end with API, such as ServiceDocumentAPI.  Requests are routed to the relevant API handler by servlets whose only task is to respond to HTTP requests by loading the relevant server implementation of the *Manager interfaces, and routing the HTTP request to the relevant method of the *API object.  This means that implementers are free to write their UI/URL space as they choose, by overriding the standard servlets as necessary.

### Standard servlets

These can be found in org.swordapp.server.servlets

    CollectionServletDefault
    ContainerServletDefault
    MediaResourceServletDefault
    ServiceDocumentServletDefault
    StatementServletDefault

Each of these implements the relevant doGet, doPost, doPut or doDelete HTTP methods, loads the relevant *Manager implementations (see above), and routes requests into the web API handlers in the library.  For example:

    GET on ServiceDocument-IRI -> ServiceDocumentServletDefault.doGet() 
        -> ServiceDocumentAPI.get() 
        -> ServiceDocumentManagerImpl.getServiceDocument()

### Custom servlets

If the standard servlets do not fit your requirements for your URL space or the way in which you want to present SWORD to your users, they can be replaced.  Their only job is to load your *Manager implementations and pass the requests to the *API objects.  The *API objects reflect exactly the requirements of the SWORD specification so failure to call them correctly will result in incompatibility with the specification.  Implementers should therefore be sure to invoke the correct API calls when routing from custom servlets.  It is recommended to use the dfault servlets as a reference.

### web.xml for deploying servlets

The library includes an example web.xml for your webapp in

    src/main/webapp/WEB-INF/web.xml

It shows how to configure your implementations of the *Manager interfaces for use in the default servlets (if you have implemented your own servlets, this may differ):

    <context-param>
        <param-name>service-document-impl</param-name>
        <param-value>org.swordapp.server.ServiceDocumentManagerImpl</param-value>
        <description>
            The ServiceDocumentManager server implementation class name
        </description>
    </context-param>

We load the implementation as a context parameter in the webapp, which the default servlets then load from the context environment.

We can then bind the servlet implementations by declaring them:

    <servlet>
        <servlet-name>servicedocument</servlet-name>
        <servlet-class>org.swordapp.server.servlets.ServiceDocumentServletDefault</servlet-class>
    </servlet>

And then mapping them to the URL space:

    <servlet-mapping>
        <servlet-name>servicedocument</servlet-name>
        <url-pattern>/servicedocument/*</url-pattern>
    </servlet-mapping>

Once this has been done, the the *Manager interfaces implemented, you are good to go with using SWORDv2!
