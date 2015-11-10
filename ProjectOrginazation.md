# Project Organization #
WiseMatches is a Java Web project that uses Maven build tool and have Maven project organization. WiseMatches is multi-modules Maven project with the following modules and additional folders:

![http://wisematches.googlecode.com/hg/design/images/ProjectOrganization.svg](http://wisematches.googlecode.com/hg/design/images/ProjectOrganization.svg)

There are a few main folders:
  * **resources** - contains design documents in Visual Paradigm and also exported to images variant of the design which can be used in WiKi pages and so on.
    * **database** - contains SQL scripts and database definition;
    * **model** - contains VisualParadigm model and exports from the model;
    * **images** - contains original images in GIMP image editor application.
    * **dictionaries** - original dictionaries for Scribble
  * **modules** - is a folder with project modules
    * **android** - contains client code for Android platform;
    * **database** - contains database configuration and base classes for database manipulation;
    * **personality** - contains classes and interfaces that describes account/person and players;
    * **playground** - contains base interfaces and classes for game play but don't have concrete implementation of game board;
    * **scribble** - contains implementation and related things for Scribble game;
    * **web** - contains implementation of web server.

# Libraries&Frameworks #
WiseMatches has been started to study new technologies and modern libraries. We are using the following libraries:
  * [Spring](http://www.springsource.org) - projects build framework
  * [Spring Security](http://static.springsource.org/spring-security/site/index.html) - security extension for Spring
  * [Hibernate](http://www.hibernate.org) - Java ORM framework
  * [Hibernate Validator](http://www.hibernate.org/subprojects/validator.html) - JSR 303 Hibernate Validator
  * [FreeMarker](http://freemarker.sourceforge.net) - template library for HTML generation (and not only)
  * [jQuery](http://jquery.com) - JavaScript library
  * [jQuery UI](http://jqueryui.com) - JavaScript UI library
  * [Maven2](http://maven.apache.org/) - Java build tool
  * [JUnit](http://www.junit.org) - test framework
  * [EasyMock](http://easymock.org) - mock objects generation framework.

# Tools #
In our work we are using some open source and free community tools:
  * [IntelliJ IDEA](http://www.jetbrains.com/idea) - The Most Intelligent Java IDE
  * [Visual Paradigm for UML](http://www.visual-paradigm.com/product/vpuml/) - UML tool for software application development