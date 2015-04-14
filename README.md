JXMPP
=====

[![Build Status](https://travis-ci.org/igniterealtime/jxmpp.svg)](https://travis-ci.org/igniterealtime/jxmpp)  [![Project Stats](https://www.openhub.net/p/jxmpp/widgets/project_thin_badge.gif)](https://www.openhub.net/p/jxmpp)

About
-----

JXMPP is an Open Source Java base library for XMPP. It provides often
used functionality needed to build an XMPP stack.

Resources
---------

- Nightly Javadoc: http://jxmpp.org/nightly/javadoc/
- Maven Releases: https://oss.sonatype.org/content/repositories/releases/org/jxmpp
- Maven Snapshots: https://oss.sonatype.org/content/repositories/snapshots/org/jxmpp

JXMPP Components
================

JXMPP consists of several components, you can use all of them or decide to pick only the ones that suit your needs.

jxmpp-core
----------

Provides core functionality most software that interacts with XMPP requires.
Highlights include

### XmppDateTime

A utility to parse date/time Strings in the various formats used within XMPP to a Date instance in a efficient manner without depending on third party libraries

### XmppStringUtils

Contains various String operations often needed when using XMPP

### XmppStringPrepUtil

A utility to apply the various string transformation profiles required by XMPP in a efficient manner. Does also utilize a Cache for maximum efficiency.


jxmpp-jid
---------

Abstracts XMPP JIDs with Java classes, performing string preparation and validation.
Although JIDs are split over five classes, jxmpp-jid is designed so that you often only need to use the simple `Jid` type.
It therefore combines the simplicity of the single JID class approach, where JIDs are represented by a single class no matter what kind of JID they are, which the expressiveness of the JID class hierarchy approach, where you can express in a method signature the required JID type (e.g. `foo(BareJid bareJid)`.

```text
         ,___Jid___,
        /    / \    \
  FullJid   /   \  DomainFullJid
      BareJid  DomainBareJid
```

There are also the `JidWithLocalpart` and `JidWithResourepart` interfaces, which are implemented by the Jid types as follows:

```text
     JidWithLocalpart       JidWithResourcepart
         /    \                   /   \
        /      \                 /     \
    BareJid  FullJid         FullJid  DomainFullJid
```

Jid instances are created with the help of the `JidCreate` utility:

```java
Jid jid = JidCreate.from("foo@bar.example");
```

jxmpp-stringprep-libidn
-----------------------

Perform XMPP's StringPrep with the help of libidn.

Use `LibIdnXmppStringprep.setup()` to make `XmppStringPrepUtil` use libidn.

jxmpp-util-cache
----------------

Provides a lightweight and efficient Cache without external dependencies used by various JXMPP Components.
