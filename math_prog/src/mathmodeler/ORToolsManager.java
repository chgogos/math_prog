package mathmodeler;

public class ORToolsManager {

	public static void loadLibraries() {
		String os_name = System.getProperty("os.name").toLowerCase();
		String os_arch = System.getProperty("os.arch").toLowerCase();
		System.loadLibrary("jniortools");
		// if (os_name.indexOf("win") >= 0) {
		// if (os_arch.indexOf("64") > 0) {
		// System.loadLibrary("ortools//win7_x86_64//jniortools");
		// } else {
		// System.loadLibrary("ortools//win7_i386//jniortools");
		// }
		// } else if (os_name.indexOf("linux") >= 0) {
		// if (os_arch.indexOf("64") > 0) {
		// System.loadLibrary("jniortools_x86_64");
		// } else {
		// System.loadLibrary("jniortools_i386");
		// }
		// } else if (os_name.indexOf("mac") >= 0) {
		// System.loadLibrary("jniortools");
		// } else {
		// System.out.println("unknown OS " + os_name);
		// }

	}
}
