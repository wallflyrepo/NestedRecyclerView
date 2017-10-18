# NestedRecyclerView
All the benefits of RecyclerView with NestedScrollView


## How to use ##


Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```
Step 2. Add the dependency
```groovy
dependencies {
	compile 'wallfly.android:nestedrecyclerview:1.0'
}
```
Step 3. Add NestedRecyclerView to your layout file
```xml
<nestedrecyclerview.wallfly.com.nestedrecyclerview.NestedRecyclerView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
```  
        