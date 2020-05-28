package com.wayyue.tracer.core.reporter.stat.model;

/**
 * StatKey
 *
 */
public class StatKey {

    /**
     * Key for statistics
     */
    private String  key;

    /**
     * Y success，N failure
     */
    private String  result;

    /**
     * Whether it is pressure measurement stat
     */
    private boolean loadTest;

    /**
     * Printed end
     */
    private String  end;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEnd() {
        return end;
    }

    public boolean isLoadTest() {
        return loadTest;
    }

    public void setLoadTest(boolean loadTest) {
        this.loadTest = loadTest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatKey statKey = (StatKey) o;

        if (key != null ? !key.equals(statKey.key) : statKey.key != null) {
            return false;
        }
        if (loadTest != statKey.loadTest) {
            return false;
        }
        if (result != null ? !result.equals(statKey.result) : statKey.result != null) {
            return false;
        }
        return end != null ? end.equals(statKey.end) : statKey.end == null;
    }

    @Override
    public int hashCode() {
        int result1 = key != null ? key.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (loadTest ? 1 : 0);
        result1 = 31 * result1 + (end != null ? end.hashCode() : 0);
        return result1;
    }
}
