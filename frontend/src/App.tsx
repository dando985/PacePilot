import {useEffect, useState} from 'react';

import {getAthleteProfiles} from './api/athletes';
import CreateAthleteProfileForm from './components/CreateAthleteProfileForm';
import TrainingPlansPanel from './components/TrainingPlansPanel';

import {ApiError} from './api/client';
import type {AthleteProfile} from './api/types';

import './App.css';

type ProfileState =
    | {status: 'loading'}
    | {status: 'success'; profiles: AthleteProfile[]}
    | {status: 'error'; message: string};

export default function App() {
  const [profileState, setProfileState] = useState<ProfileState>({status: 'loading',});
  const [reloadCount, setReloadCount] = useState(0);
  const [selectedAthleteId, setSelectedAthleteId] = useState<string | null>(null,);

  // Load athlete profiles when the component mounts or when reloadCount changes.
  useEffect(() => {
    const controller = new AbortController();

    // Function to load athlete profiles from the API.
    async function loadProfiles() {
      try {
        // Fetch athlete profiles from the API.
        const profiles = await getAthleteProfiles(controller.signal);

        if (!controller.signal.aborted) {
          setProfileState({status: 'success', profiles,});
        }
      } catch (error: unknown) {
        if (controller.signal.aborted) {
          return;
        }

        setProfileState({
          status: 'error',
          message:
              error instanceof ApiError
                  ? error.message
                  : 'Unable to load athlete profiles. Please try again.',
        });
      }
    }

    // Call the function to load athlete profiles.
    void loadProfiles();

    // Cleanup function to abort the fetch request if the component unmounts or reloadCount changes.
    return () => {controller.abort();};
  }, [reloadCount]);

  // Function to reload athlete profiles by incrementing the reloadCount state.
  function reloadProfiles() {
    setProfileState({ status: 'loading' });
    setReloadCount((count) => count + 1);
  }

  return (
      <main className="app">
        <header className="app-header">
          <div>
            <p className="app-name">PacePilot</p>
            <h1>Athlete profiles</h1>
          </div>

          <button type="button" onClick={reloadProfiles} disabled={profileState.status === 'loading'}>
            {profileState.status === 'error' ? 'Retry' : 'Refresh'}
          </button>
        </header>

        <CreateAthleteProfileForm onCreated={reloadProfiles} />

        {profileState.status === 'loading' && (<p role="status">Loading athlete profiles...</p>)}

        {profileState.status === 'error' && (<p role="alert">{profileState.message}</p>)}

        {profileState.status === 'success' && (
            profileState.profiles.length === 0 ? (
                <p>No athlete profiles yet.</p>
            ) : (
                <ul className="profile-list">
                  {profileState.profiles.map((profile) => (
                      <li key={profile.id} className="profile-card">
                        <h2>{profile.displayName}</h2>

                        <p>
                          Training days per week: {profile.availableTrainingDaysPerWeek}
                        </p>

                        <button
                            type="button"
                            onClick={() => setSelectedAthleteId(profile.id)}
                            aria-pressed={selectedAthleteId === profile.id}
                        >
                          View training plans
                        </button>
                      </li>
                  ))}
                </ul>
            )
        )}

        {selectedAthleteId !== null && (<TrainingPlansPanel key={selectedAthleteId} athleteId={selectedAthleteId}/>)}
      </main>
  );
}