#Why I build a v2ex app whereas there is such app exists

I visit v2ex website everyday, but there's no official app yet. My purpose is to build a adaptive ui app for both mobile phones and tablets.
Since there are basic open apis for me to build a small app easily, and I will add new features as long as there are more apis to use.

This app is inspired by a presentation by Virgil Dobjanschi at Google I/O 2010 and google iosched 2014 sample app.
Virgil Dobjanschi represented three modern design patterns without code, they are:

- Option A: Use a service api.
- Option B: Use the contentProvider api.
- Option C: Use the contentProvider and sync adapter.

I choose option c to build my app. These design patterns mainly focus on how to store and retrieve data in the background.
First of all, there’s no doubt that we store the data fetched from server in local sqlite database.
Secondly, we all know not to block main ui.

But how to do it? One of the best practices is: CursorLoader + ContentProvider + SyncAdapter.

 ![Use contentProvider and syncAdapter api design pattern](https://github.com/taoliuh/v2ex/blob/wip/doc/option-c.png "Use contentProvider and syncAdapter api design pattern")

And this is the incorrect way to implement REST methods.

 ![The Incorrect Implementation of REST Methods](https://github.com/taoliuh/v2ex/blob/wip/doc/incorrect_way_to_implement_rest_methods.png "The Incorrect Implementation of REST Methods")

#Features

- The usage of RecyclerView, implement CursorLoader for RecyclerView, add header and footer to RecyclerView and so on.
- The usage of sync adapter, how to initial a network request manually using sync adapter.
    And the usage of powerful ability of sync adapter to execute schedule task.
- The template code of using contentProvider.

#Conclusion

- Do not implement REST methods inside Activities.
- Start long running operations from a Service.
- Persist early & persist often.
- Minimize the network usage.