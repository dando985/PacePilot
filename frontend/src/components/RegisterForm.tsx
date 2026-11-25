import {useRef, useState} from 'react';

import {registerAccount} from '../api/auth';
import {ApiError} from '../api/client';

import type {UserAccount} from '../api/types';

type SubmissionState =
    | {status: 'idle'}
    | {status: 'submitting'}
    | {status: 'error'; message: string}
    | {status: 'success'; message: string};

export default function RegisterForm(props: {onShowLogin: () => void}) {
    const [submission, setSubmission] = useState<SubmissionState>({status: 'idle'});
    const [showPasswords, setShowPasswords] = useState(false);

    const pending = useRef(false);
    const submitting = submission.status === 'submitting';

    function showError(message: string) {
        setSubmission({status: 'error', message});
    }

    async function handleSubmit(form: HTMLFormElement) {
        if (pending.current || !form.reportValidity()) {
            return;
        }

        const data = new FormData(form);
        const email = String(data.get('email') ?? '').trim();
        const password = String(data.get('password') ?? '');
        const confirmPassword = String(data.get('confirmPassword') ?? '');

        if (!email || !password.trim()) {
            showError('Enter your email and password.');
            return;
        }

        if (password.length < 6 || password.length > 72) {
            showError('Password must contain between 6 and 72 characters.');
            return;
        }

        if (new TextEncoder().encode(password).length > 72) {
            showError(
                'Password is too long. Some characters, such as emoji, take more space. Try a shorter password.');
            return;
        }

        if (password !== confirmPassword) {
            showError('Passwords do not match.');
            return;
        }

        pending.current = true;
        setSubmission({status: 'submitting'});

        let account: UserAccount;

        try {
            account = await registerAccount({email, password});
        } catch (error: unknown) {
            showError(
                error instanceof ApiError ? error.message : 'Unable to confirm account creation. Please try again or try signing in.'
            );
            return;
        } finally {
            pending.current = false;
        }

        form.reset();
        setShowPasswords(false);
        setSubmission({status: 'success', message: `Account created for ${account.email}. Sign in to continue.`});
    }

    if (submission.status === 'success') {
        return (
            <section className="auth-form" aria-label="Account created">
                <h2>Account created.</h2>

                <p role="status">{submission.message}</p>

                <div className="auth-form-actions">
                    <button type="button" onClick={props.onShowLogin}>
                        Sign in
                    </button>
                </div>
            </section>
        );
    }

    return (
        <form className="auth-form" aria-label="Create account" aria-busy={submitting} onSubmit={(event) => {
                event.preventDefault();void handleSubmit(event.currentTarget);}}>
            <h2>Create your account.</h2>

            <p>
                Create a PacePilot account to build training plans, log your runs,
                and get coaching along the way.
            </p>

            {submission.status === 'error' && (<p role="alert">{submission.message}</p>)}

            <fieldset disabled={submitting}>
                <legend>Account details</legend>

                <p>
                    <label>
                        Email
                        <input name="email" type="email" autoComplete="username" autoCapitalize="none" spellCheck={false}
                            maxLength={254} required/>
                    </label>
                </p>

                <p>
                    <label>
                        Password
                        <input id="register-password" name="password" type={showPasswords ? 'text' : 'password'}
                            autoComplete="new-password" autoCapitalize="none" spellCheck={false} minLength={6}
                            aria-describedby="register-password-help" required/>
                    </label>
                </p>

                <p id="register-password-help">
                    The password should be 6 to 72 characters long.
                </p>

                <p>
                    <label>
                        Confirm password
                        <input id="register-confirm-password" name="confirmPassword" type={showPasswords ? 'text' : 'password'}
                            autoComplete="new-password" autoCapitalize="none" spellCheck={false} required/>
                    </label>
                </p>

                <button className="password-toggle" type="button" aria-controls="register-password register-confirm-password"
                    onClick={() => setShowPasswords((current) => !current)}>
                    {showPasswords ? 'Hide passwords' : 'Show passwords'}
                </button>
            </fieldset>

            <div className="auth-form-actions">
                <button type="submit" disabled={submitting}>
                    {submitting ? 'Creating account...' : 'Create account'}
                </button>

                <button type="button" onClick={props.onShowLogin} disabled={submitting}>
                    Back to sign in
                </button>
            </div>
        </form>
    );
}