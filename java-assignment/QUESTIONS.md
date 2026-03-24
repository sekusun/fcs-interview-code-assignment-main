# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**

```txt
The code base has 2 implementation stratergies used for the data access layer - Store & Product uses Panache, where as Warehouse uses domain, ports model (Hexagonal Architecture). Both has its own advantages and it depends on the situation to be considered for using. 

Panache is a Quarkus specific library method which reduces the boiler plate code by keeping the entities open, sitting on top of standard JPA. So, we have full access to the raw JPA when needed and easy to understand (less referring of files). This library offers compact/conveninet methods for basic CRUD operations which makes the database layer management clean & concise. Given the situation of complex domain logic, it can be harder to debug and can be error prone at run time. Since it uses string based dynamic queries, if string fragment used has some mistake that cannot be identified at compile time and lead to runtime error.

Ports & adapters follows a loosly coupled style/pattern, which allows Interfaces (Ports) and Implementations (adapters). This method is easy for a dynamic environment keeping external dependencies flow through abstract interfaces. This allows independent, testable and easy to evolve. Using this method we can easily change the adapter(database) for the same business logic without much hassle. 

Obviously in a single repository, having same or consistent style helps the code legible. Given the choice from these two, the option to select is based on the usage & purpose. If there is no complex logic expected, Panache would be good by saving the time. However in unknown and safer case, Hexagonal architecture helps in the long run due to its advantage. Still refactoring to one format is not that complex for this code base.
```

---

1. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**

```txt
The difference between both the approach are mainly about WHEN & HOW the contract is defined. 

One can start with OpenAPI spec defining paths, methods, parameters etc.. then implement the code based on the spec considering it as guardrail and implement based on the given ask. This is slower at the beginning, but will achieve bette consistency and cleaner design especially for multi team projects. Frontend team can focus on their logic as the contract is fixed and QA team can focus on the match for given spec. In this approach a single mistake can cause more time/cost.

Code first approach is the traditional/old fashioned (mostly a natural way), one can begin with the logic in mind and create the annotations and methods at first then following with the code. If the team is packed with fullstack people, they can refer the code without hassle and a simple change can be understandable.

Finally the choice between Reactive (code first) and Proactive (Spec first) approach depends on the usecase, in some POC's we cannot wait for the spec to be completed where as for a fullfledged real production implementation we cannot start just with code. Ideal preferrence in an organisation is better to go with Spec first approach to have the safer and less iterative problems reducing back and forth discussions. 
```

---

1. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**

```txt
In any backend or API code, testing is a must and inevitable one to miss. A very minimum testing should have unit and integration testing. Unit test covers each bits and pieces of the methods that identifies the small errors quickly as it is being tested with mock values keeping only the method signature as referrance like stopping the small leakages. In some cases, unit test can also be used to check the performance by mocking the right load as parameters.

The next level would be Integration testing, where covering the end to end flow at a time and iteratively for all the possible/expected HTTP codes. the whole business cases/journeys can be tested in this method. Any organisation should have the coverage of the testing as high as possible to keep the healthy usecases without repeating the conditions for the sake/formality. These test can also be triggered/validated for all the PR's and can fail in case of lower coverage.

Further, the next one can be perforance or stress/load testing. For a high consumer environment, to cover the consistent output in time for all concurrent calls are much important. each milli second improvements in a long run will help in healthy business keeping the cost less.

All the above testings (Unit, Integration & Performance) can be integrated with the pipeline and set the standard way of practice.
```

