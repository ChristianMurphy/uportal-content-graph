# uPortal Content Graph

Demonstrator project for a small data management library developed by Foothill-De Anza as part of its upcoming portal refresh project.

The desire for the content graph arose out of appreciation for the DLM (distributed layout manager) and how it handles multiple disperate kinds of structured data that can be _assigned_ to a user. Content graph jumps in to address the lack of extensibility presently available to uPortal implementors. One example might be that an implementor cannot easily define new institution-specific objects. With these custom objects defined, then the content graph can be make them available to multiple portlets (or in the future APIs) for a wide variety of uses.

Another goal of the content graph is to achieve higher developer productivity by not being encumbered with the need to write and maintain large XML documents.

Being a concept project, for the time being this will not be published to Maven where it can be pulled into another portal project. The plan here is just to make the source code available, and start some conversations on how similar functionality can be incorporated into future uPortal versions.

## Usage

A few simple test cases are included for demonstration.

```
./gradlew clean test
```

