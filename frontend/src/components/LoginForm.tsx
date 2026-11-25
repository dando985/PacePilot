import {useRef, useState} from 'react';

import {getCurrentAccount, login} from '../api/auth';
import {ApiError} from '../api/client';

import type {UserAccount} from '../api/types';

type SubmissionState =
    | {status: 'idle'}
    | {status: 'submitting'}
    | {status: 'error'; message: string};

export default function LoginForm(props: {onLoggedIn: (account: UserAccount) => void; onShowRegistration: () => void}) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const pending = useRef(false);
    const submitting = submission.status === 'submitting';

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const email = String(data.get('email') ?? '').trim();
        const password = String(data.get('password') ?? '');

        if (!email || !password) {
            setSubmission({status: 'error', message: 'Enter your email and password.'});
            return;
        }

        pending.current = true;
        setSubmission({status: 'submitting'});

        let account: UserAccount | null;

        try {
            await login({email, password});
            account = await getCurrentAccount();
        } catch (error: unknown) {
            setSubmission({
                status: 'error',
                message:
                    error instanceof ApiError && error.status === 401
                        ? 'Email or password is incorrect.' : 'Unable to confirm sign-in. Please try again.',
            });
            return;
        } finally {
            pending.current = false;
        }

        if (account === null) {
            setSubmission({status: 'error', message: 'Unable to confirm sign-in. Please try again.'});
            return;
        }

        form.reset();
        setSubmission({status: 'idle'});
        props.onLoggedIn(account);
    }

    return (
        <form className="auth-form" aria-label="Sign in" aria-busy={submitting} onSubmit={(event) => {
                event.preventDefault();
                void handleSubmit(event.currentTarget);
            }}
        >
            <h2>Sign in</h2>

            <fieldset disabled={submitting}>
                <legend>Account details</legend>

                <p>
                    <label>
                        Email
                        <input name="email" type="email" autoComplete="username" maxLength={254} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Password
                        <input name="password" type="password" autoComplete="current-password" required/>
                    </label>
                </p>
            </fieldset>

            <div className="auth-form-actions">
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Signing in...' : 'Sign in'}
                </button>

                <button type="button" onClick={props.onShowRegistration} disabled={submitting}>
                    Create an account
                </button>
            </div>

            {submission.status === 'error' && (<p role="alert">{submission.message}</p>)}
        </form>
    );
}