## Description ##

JDiffPatch compares two objects and creates a DiffRecord which contains the old and new value

This DiffRecord can be used to patch any object of the same kind

## Usage ##

```

JDiff diffCreator = new JDiff();

DiffRecord diffRecord = diffCreator.compare(newObject, oldObject);

assert oldObject.getText() != newObject.getText();

JPatch patcher = new JPatch(oldObject);
patcher.patch(diffRecord);

assert oldObject.getText() == newObject.getText();

```