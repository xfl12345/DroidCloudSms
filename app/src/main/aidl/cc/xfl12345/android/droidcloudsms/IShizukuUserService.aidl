// IShizukuUserService.aidl
package cc.xfl12345.android.droidcloudsms;

// Declare any non-default types here with import statements

interface IShizukuUserService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void justTest() = 1;

    void destroy() = 16777114; // Destroy method defined by Shizuku server

}