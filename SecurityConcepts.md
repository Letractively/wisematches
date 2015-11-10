# Overview #
In our project we are using open source [Spring Security](http://static.springsource.org/spring-security/site/index.html) framework. The best way to get more about Spring Security is read excellent book [Spring Security 3](http://www.springsecuritybook.com) by [Peter Mularien](http://www.mularien.com/blog/).

This page describes user roles, data storage, passwords encryption and so on.

# User Roles #
There are a few roles a three security separation levels:
  1. **Resources access level**. Base roles which separates access to resources like URLs. There are three roles:
    * _anonymous_ - not authorized user. Has access only to information services and web pages. Don't have access to part of the site. Only to info and register pages;
    * _user_ - any authorized user who has been registered and signed in. Has access to all pages except administration pages;
    * _admin_ - the administrator of the site. Has access to administration pages.
  1. **Content access level**. Two roles which separate content of security pages.
    * _member_ - the registered member with payed account. At this moment is not used.
    * _guest_ - the guest account. Has limited access to some features and volume.
  1. **Volume access level**. A few roles that limit volume of the services, like number of concurrent games, number of concurrent tournaments and so on.
    * _silver_ - restriction is not defined yet.
    * _gold_ - restriction is not defined yet.
    * _platinum_ - restriction is not defined yet.

This is shown on the following diagram:



&lt;object type="image/svg+xml" data="http://wisematches.googlecode.com/hg/design/export/UserRoles.svg"&gt;



If one role extends another one it means that both roles are assigned to the user.

# Data Storage #
TBD

# Password Encryption #
TBD

# Remember Me #
TBD