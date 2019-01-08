package com.xgame.ui.activity.home;

import java.lang.ref.WeakReference;

public interface MailBox {

    void onMailReceive(MailMessage msg);

    class MailMessage {

        public int what;

        public int arg1;

        public int arg2;

        public Object obj;

        private WeakReference<Runnable> replyRef;

        public static MailMessage create(int what) {
            MailMessage msg = new MailMessage();
            msg.what = what;
            return msg;
        }

        public static MailMessage create(int what, int... args) {
            MailMessage msg = new MailMessage();
            msg.what = what;
            int length;
            if (args != null && (length = args.length) > 0) {
                msg.arg1 = args[0];
                msg.arg2 = length > 1 ? args[1] : 0;
            }
            return msg;
        }

        public void reply() {
            Runnable r;
            if (replyRef != null && (r = replyRef.get()) != null) {
                r.run();
            }
        }

        public MailMessage autoReply(Runnable func) {
            replyRef = new WeakReference<>(func);
            return this;
        }

        public MailMessage object(Object obj) {
            this.obj = obj;
            return this;
        }
    }
}