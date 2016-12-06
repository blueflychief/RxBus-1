package vite.rxbus;

import java.util.Set;

import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.observers.SerializedSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by trs on 16-11-29.
 */

public class SubjectKeeper {
    private Subject subject;
    private Subscription subscription;

    private boolean isRelease = false;

    public SubjectKeeper(Scheduler scheduler, Action1<? extends Object> action) {
        subject = new SerializedSubject(PublishSubject.create());
        subscription = subject.
                subscribeOn(scheduler).
                subscribe(action,
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                                subject.onCompleted();

                                //TODO: should i register again?
                            }
                        });
    }

    /**
     * @param value
     * @return false - need delete
     */
    public boolean post(Object value) {
        if (!isRelease) {
            subject.onNext(value);
        }
        return !isRelease;
    }

    public void release() {
        isRelease = true;
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = null;
        subject = null;
    }
}