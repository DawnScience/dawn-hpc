This project contains an OSGI-bundled DRMAA API implementation.

Following changes have been applied :
- all collections in the API have been specified with their generic type(s)
- serialVersionUIDs have been generated for all Serializables to make my eclipse IDE happy
- SessionFactory.setInstance has been added to allow a specific DRM impl bundle to register its SessionFactoryImpl.
This allows maintaining the standard DRMAA client-side-approach to use SessionFactory.getInstance() singleton lookups,
i.o. dependency-injection-based approaches.