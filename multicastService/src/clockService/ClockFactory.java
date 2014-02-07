/*
 * Author: Tao yu, Minglei Chen 
 * 
 * ClockFactory used to create a clock service
 */
package clockService;

public class ClockFactory {
	public ClockService createClockService(ClockType clockType, int size, int index) {
		if (clockType == ClockType.LOGICAL) {
			return new LogicalClockService();
		}
		else if (clockType == ClockType.VECTOR) {
			return new VectorClockService(size, index);
		}
		return null;
	}
}
