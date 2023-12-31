# Hobbes Design  

## Front-end and IR

### Symbol Table
Our symbol table implementation roughly follows the sheaf-of-tables description from [1] (including method names e.g. InitializeScope/FinalizeScope, insert/lookup).  We differ a bit in that we define a single symbol table that manages a tree of Scope objects which point to each other.  As we walk the parse tree we call InitializeScope to create a new scope and FinalizeScope to pop out of the current scope into its parent.  Symbols for each scope are all stored in the same hash map inside a Scope object.  The symbol table is built via a tree visitor SemanticVisitor.

### Semantic Analysis

Semantic checking is implemented in the same SemanticVisitor used to construct the symbol table.  The semantic checking largely involves looking up type information in the symbol table and doing comparisons, resolutions, etc.  We have a set of twenty distinct semantic error types that have their own specific report message (see com.hobbes.semantic.SemanticError{Type}).

### IR Generation
IR generation is performed in a separate visitor class called IRVisitor.  The IRVisitor is supplied with the symbol table for looking up types for temporary variable creation, but is otherwise free of semantic code (which is nice).  The IRVisitor walks the symbol table in a read-only manner (calls enterScope/exitScope as opposed to InitializeScope/FinalizeScope).  A linear IR program is created by walking the parse tree.  Our IR abstraction is for the most part simply a list of IR statements (which can either be an operation or a location identifier for jumps) and the associated variables.  A large hierarchy of IR classes for program representation and operation definitions can be found under com.hobbes.ir.  

### Known Issues

* In our symbol table, we are keeping all of our symbols of different types (which implement the Symbol interface) in the same map structure.  Our semantic phase does not do any validation across defined types within the symbol table s.t. if a function were defined and later was attempted to be used as a different symbol (viz. variable) our semantic checker would not detect this.  The plan is to refactor the symbol table to treat symbol types separately s.t. lookup is over a fixed symbol type.

* Temporary variables from the IR generation are not placed in the symbol table.  This was described as optional, and was easier for the two-pass implementation.

* No handling of negative exponents at the moment (though we do handle the 0-exponent case).


## Backend and Code Generation  

### MIPS Function Calling Convention

For simplicity, we pass all function arguments on the stack (with the associated performance penalty).  Our register allocators bias towards allocating temp registers (as opposed to save), placing most of the responsibility for cross-call consistency on the caller.


### Register Allocation Data Structures

The three register allocators implement an interface called RegisterAllocator (*com.hobbes.backend.allocation.BaseRegisterAllocator*) which declares a void method named *run* for running the respective allocation algorithm.  Each allocator type extends a BaseRegisterAllocator class (*com.hobbes.backend.allocation.BaseRegisterAllocator*) which implements much of the core logic for register assignment, temporary allocation, etc.  Register allocation is tracked via a hash map from the allocated variable to its assigned register.  In each we pass in the associated function context via an IRFunction object.

#### Naive

The *run* method for the Naive allocator is a no-op.  All functionality is inherited from the BaseRegisterAllocator.

#### Local Intrablock

The local intrablock allocation algorithm is run on each basic block of a control flow graph (CFG) for a function.  It simply orders the variables found in the basic block by their use counts and allocates registers greedily.  The MIPS code for each basic block is generated by walking the CFG in the MIPSProgram class (*com.hobbes.backend.mips.MIPSProgram*).

#### Global Briggs

The global Briggs-style allocator takes an InterferenceGraph object (*com.hobbes.backen.liveness.InterferenceGraph*).  This graph contains a list of Web objects (*com.hobbes.backend.liveness.Web*) as nodes and maintains an adjacency list (via a hash map) where one web is in another web's adjacency list if they have any overlapping live ranges.  The Briggs' allocation algorithm requires a stack object and we iterate through/mutate the interference graph while populating the stack.

### Known issues

* Our mechanism for passing/receiving function arguments via registers experienced clobbering in certain scenarios so it is disabled by default.  All function arguments are thus passed via the stack (and hence the horrendous absolute TAK scores).
* Our spill cost calcuation (which is utilized in the Briggs' allocation algorithm) does not have the loop depth of the respective statement made available to it and is thus a sub-optimal proxy.  We could implement a CFG walk to analyze loop structures in the linear IR or could store loop depths in our IR statements (inelegant) via the IRVisitor.


## References  
[1] Engineering a Compiler, 2nd Edition by Keith D. Cooper and Linda Torczon 