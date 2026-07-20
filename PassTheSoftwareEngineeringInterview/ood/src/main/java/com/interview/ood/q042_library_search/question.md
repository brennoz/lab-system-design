**Lessons:** [0002 Library Search: Four Layers, One Interview](../../../../../../../../topics/ood/library-system/0002-search-strategies.html)

# q042 — Library Search (NatWest Retro)

**Category:** OOD · **Difficulty:** Easy-Medium · **Ideal time:** 20 min

---

## Ticket (verbatim, as given)

> As a user I would like to be able to search library books by title and author.

---

## Given code (exactly what was handed over)

```java
// Book.java — given, do not change
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Book {
    private final String title;
    private final String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(title).toString();
    }
}
```

```java
// Library.java — given stub, this is what must be fixed
public class Library {
    private final List<Book> books = new ArrayList<>();

    public List<Book> search(String searchString) {
        return books;   // BUG: ignores searchString entirely
    }

    public void add(Book book) {
        books.add(book);
    }
}
```

```java
// LibraryTest.java — given, DO NOT edit; this is the spec
class LibraryTest {
    private static final Book LORD_OF_THE_FLIES = new Book("Lord of the Flies", "Fred Perry");
    private static final Book LORD_OF_THE_RINGS = new Book("Lord of the Rings", "John Smith");
    private static final Book WAR_AND_PEACE     = new Book("War and peace", "Lord Bennett");
    private static final Book HARRY_POTTER      = new Book("Harry Potter", "J.K. Rowling");

    private final Library library = new Library();

    @BeforeEach
    void initialise() {
        library.add(LORD_OF_THE_FLIES);
        library.add(LORD_OF_THE_RINGS);
        library.add(WAR_AND_PEACE);
    }

    @Test
    void searchReturnsTheSingleBookThatMatchesTheEntireTitle() {
        var results = library.search("Lord of the Flies");
        assertEquals(1, results.size(), "matching books");
        var book = results.get(0);
        assertSame(LORD_OF_THE_FLIES, book);
        assertEquals("Lord of the Flies", book.getTitle());
        assertEquals(0, library.search("no book").size(), "matching books");
    }

    @Test
    void searchReturnsSingleBookThatMatchesCaseInsensitiveTitle() {
        var results = library.search("lord of the flies");
        assertEquals(1, results.size(), "matching books");
        assertEquals("Lord of the Flies", results.get(0).getTitle());
    }

    @Test
    void searchReturnsBookMatchingTitleOrAuthor() {
        var results = library.search("lord");
        assertEquals(3, results.size(), "matching books");
        assertTrue(results.contains(LORD_OF_THE_FLIES));
        assertTrue(results.contains(LORD_OF_THE_RINGS));
        assertTrue(results.contains(WAR_AND_PEACE));
        assertFalse(results.contains(HARRY_POTTER));
    }
}
```

`pom.xml` (given) declared `commons-lang3`, `junit-jupiter`, `hamcrest-all`, `assertj-core`,
`mockito-core`, `lombok` on the classpath. `commons-lang3` being present and never imported
is the tell — `StringUtils.containsIgnoreCase` is the expected tool.

---

## What went wrong live (retro)

**The bug:** `search()` never reads `searchString` — returns the whole catalogue every time.

**Why the freeze happened:** implementation blank under time pressure, scope uncertainty
(plain filter vs a pattern), and a calibration gap against richer library models already
built in this project (`q030_library_system`, `ex4_library_borrowing`) — that richer model
wasn't wasted effort, see the linked lesson's section 5 for exactly how it connects.

---

## Folder layout

(Standard for `next ood` from q042 onward — see CLAUDE.md's "OOD Delivery Convention" section.)

```
q042_library_search/
├── question.md
├── solution/   ← full Specification-pattern implementation, heavily commented, TDD-ordered
└── answer/     ← empty (.gitkeep) — write from scratch, or study the lesson + solution/ first
```

---

## Hints

If stuck → type `hint`
To see full solution → type `solution`
To submit → type `done`
