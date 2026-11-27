import {useCallback, useEffect, useRef, useState} from 'react';

import {getCurrentAccount, logout} from './api/auth';
import {getCurrentAthleteProfile} from './api/athletes';
import {ApiError} from './api/client';
import AppSidebar from './components/AppSidebar';
import CreateAthleteProfileForm from './components/CreateAthleteProfileForm';
import EditAthleteProfileForm from './components/EditAthleteProfileForm';
import LoginForm from './components/LoginForm';
import RegisterForm from './components/RegisterForm';
import TrainingPlansPanel from './components/TrainingPlansPanel';

import type {AthleteProfile, ExperienceLevel, Gender, UserAccount} from './api/types';
import type {DashboardView} from './components/AppSidebar';

import './App.css';

type SessionState =
    | {status: 'loading'}
    | {status: 'anonymous'}
    | {status: 'authenticated'; account: UserAccount}
    | {status: 'signingOut'}
    | {status: 'error'; message: string; retry: 'session' | 'logout'};

type ProfileState =
    | {status: 'loading'}
    | {status: 'success'; profile: AthleteProfile | null}
    | {status: 'error'; message: string};

const genderLabels: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
  NON_BINARY: 'Non-binary',
  OTHER: 'Other',
  PREFER_NOT_TO_SAY: 'Prefer not to say',
};

const experienceLabels: Record<ExperienceLevel, string> = {
  BEGINNER: 'Beginner',
  INTERMEDIATE: 'Intermediate',
  ADVANCED: 'Advanced',
};

const birthDateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeZone: 'UTC',
});

const viewTitles: Record<DashboardView, string> = {
  dashboard: 'Dashboard',
  'training-plans': 'Training plans',
  profile: 'Profile',
};

export default function App() {
  const [session, setSession] = useState<SessionState>({status: 'loading'});
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  const [activeView, setActiveView] = useState<DashboardView>('dashboard');
  const [sessionReload, setSessionReload] = useState(0);
  const logoutPending = useRef(false);

  useEffect(() => {
    const controller = new AbortController();

    async function loadSession() {
      try {
        const account = await getCurrentAccount(controller.signal);

        if (!controller.signal.aborted) {
          setSession(account === null ? {status: 'anonymous'} : {status: 'authenticated', account});
        }
      } catch {
        if (!controller.signal.aborted) {
          setSession({status: 'error', message: 'Unable to check your session. Please try again.', retry: 'session'});
        }
      }
    }

    void loadSession();

    return () => controller.abort();
  }, [sessionReload]);

  const handleSessionExpired = useCallback((accountId: string) => {
    setSession((current) => {
      if (current.status === 'authenticated' && current.account.id === accountId) {
        return {status: 'anonymous'};
      }

      return current;
    });
  }, []);

  function reloadSession() {
    if (logoutPending.current) {
      return;
    }

    setSession({status: 'loading'});
    setSessionReload((count) => count + 1);
  }

  function handleLoggedIn(account: UserAccount) {
    setAuthMode('login');
    setActiveView('dashboard');
    setSession({status: 'authenticated', account});
  }

  async function handleLogout() {
    if (logoutPending.current) {
      return;
    }

    logoutPending.current = true;
    setSession({status: 'signingOut'});

    try {
      await logout();
      setAuthMode('login');
      setActiveView('dashboard');
      setSession({status: 'anonymous'});
    } catch {
      setSession({status: 'error', message: 'Unable to confirm sign-out. Please try again.', retry: 'logout'});
    } finally {
      logoutPending.current = false;
    }
  }

  function retrySessionRequest() {
    if (session.status === 'error' && session.retry === 'logout') {
      void handleLogout();
    } else {
      reloadSession();
    }
  }

  if (session.status === 'authenticated') {
    const account = session.account;

    return (
        <div className="app app-shell">
          <AppSidebar
              email={account.email}
              activeView={activeView}
              onNavigate={setActiveView}
              onSignOut={() => void handleLogout()}
          />

          <main className="app-main" aria-labelledby="app-page-title">
            <header className="app-header">
              <div>
                <h1 id="app-page-title">{viewTitles[activeView]}</h1>
              </div>

              <div className="account-controls">
                <button type="button" onClick={reloadSession}>
                  Refresh
                </button>
              </div>
            </header>

            <AthleteDashboard
                key={account.id}
                accountId={account.id}
                activeView={activeView}
                onNavigate={setActiveView}
                onSessionExpired={handleSessionExpired}
            />
          </main>
        </div>
    );
  }

  return (
      <main className="app">
        <header className="app-header">
          <div>
            <p className="app-name">PacePilot</p>
            <h1>Find your pace.</h1>
          </div>
        </header>

        {session.status === 'loading' && (<p role="status">Checking your session...</p>)}

        {session.status === 'signingOut' && (<p role="status">Signing out...</p>)}

        {session.status === 'error' && (
            <section>
              <p role="alert">{session.message}</p>
              <button type="button" onClick={retrySessionRequest}>
                {session.retry === 'logout' ? 'Retry sign out' : 'Retry'}
              </button>
            </section>
        )}

        {session.status === 'anonymous' && (
            authMode === 'login' ? (
                <LoginForm
                    onLoggedIn={handleLoggedIn}
                    onShowRegistration={() => setAuthMode('register')}
                />
            ) : (
                <RegisterForm onShowLogin={() => setAuthMode('login')}/>
            )
        )}
      </main>
  );
}

function AthleteDashboard(
    props: {
      accountId: string;
      activeView: DashboardView;
      onNavigate: (view: DashboardView) => void;
      onSessionExpired: (accountId: string) => void;
    }
) {
  const [profileState, setProfileState] = useState<ProfileState>({status: 'loading',});
  const [reloadCount, setReloadCount] = useState(0);
  const [editing, setEditing] = useState(false);
  const [profileMessage, setProfileMessage] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadProfile() {
      try {
        const profile = await getCurrentAthleteProfile(controller.signal);

        if (!controller.signal.aborted) {
          setProfileState({status: 'success', profile});
        }
      } catch (error: unknown) {
        if (controller.signal.aborted) {
          return;
        }

        if (error instanceof ApiError && error.status === 401) {
          props.onSessionExpired(props.accountId);
          return;
        }

        setProfileState({status: 'error', message: 'Unable to load your athlete profile. Please try again.'});
      }
    }

    void loadProfile();

    return () => controller.abort();
  }, [props.accountId, props.onSessionExpired, reloadCount]);

  function reloadProfile() {
    setEditing(false);
    setProfileMessage(null);
    setProfileState({status: 'loading'});
    setReloadCount((count) => count + 1);
  }

  function handleProfileUpdated(profile: AthleteProfile) {
    setProfileState({status: 'success', profile});
    setEditing(false);
    setProfileMessage(`Updated profile for ${profile.displayName}.`);
  }

  const profile = profileState.status === 'success' ? profileState.profile : null;

  return (
      <section className="athlete-dashboard" aria-label="Athlete dashboard">
        {profileState.status === 'loading' && (
            <p role="status">Loading your athlete profile...</p>
        )}

        {profileState.status === 'error' && (
            <div>
              <p role="alert">{profileState.message}</p>
              <button type="button" onClick={reloadProfile}>
                Retry profile
              </button>
            </div>
        )}

        {profileState.status === 'success' && profile === null && (
            props.activeView === 'profile' ? (
                <CreateAthleteProfileForm onCreated={reloadProfile}/>
            ) : (
                <section className="profile-setup">
                  <h2>Create your athlete profile</h2>
                  <p>
                    Add your athlete details to start building training
                    plans and recording workouts.
                  </p>
                  <button type="button" onClick={() => props.onNavigate('profile')}>
                    Set up profile
                  </button>
                </section>
            )
        )}

        {profile !== null && (
            <TrainingPlansPanel key={`training-plans:${profile.id}`} athleteId={profile.id} activeView={props.activeView}>
              {profileMessage !== null && (
                  <p role="status" className="profile-update-message">
                    {profileMessage}
                  </p>
              )}

              <article className="profile-card">
                <h2>{profile.displayName}</h2>

                <p>
                  Birth date
                  <br/>
                  <strong>
                    <time dateTime={profile.birthDate}>
                      {birthDateFormatter.format(
                          new Date(`${profile.birthDate}T00:00:00Z`)
                      )}
                    </time>
                  </strong>
                </p>

                <p>
                  Weight
                  <br/>
                  <strong>
                    {profile.weight}{' '}
                    {profile.weightUnit === 'KILOGRAMS' ? 'kg' : 'lb'}
                  </strong>
                </p>

                <p>
                  Gender
                  <br/>
                  <strong>{genderLabels[profile.gender]}</strong>
                </p>

                <p>
                  Experience
                  <br/>
                  <strong>
                    {experienceLabels[profile.experienceLevel]}
                  </strong>
                </p>

                {!editing && (
                    <div className="profile-card-actions">
                      <button
                          type="button"
                          onClick={() => {setProfileMessage(null);setEditing(true);}}
                      >
                        Edit profile
                      </button>
                    </div>
                )}
              </article>

              {editing && (
                  <EditAthleteProfileForm key={`edit-profile:${profile.id}`} profile={profile}
                                          onUpdated={handleProfileUpdated} onCancel={() => setEditing(false)}
                  />
              )}
            </TrainingPlansPanel>
        )}
      </section>
  );
}