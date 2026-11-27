export type DashboardView = 'dashboard' | 'training-plans' | 'profile';

const navigationItems: {view: DashboardView; label: string; iconPath: string; }[] = [
    {view: 'dashboard', label: 'Dashboard', iconPath: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z'},
    {view: 'training-plans', label: 'Training plans', iconPath: 'M9 6h12M9 12h12M9 18h12M3 6h.01M3 12h.01M3 18h.01'},
    {view: 'profile', label: 'Profile', iconPath: 'M20 21v-2a8 8 0 0 0-16 0v2M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z'},
];

function SidebarIcon(props: {path: string}) {
    return (
        <svg
            className="sidebar-icon"
             width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            focusable="false"
        >
            <path d={props.path}/>
        </svg>
    );
}

export default function AppSidebar(props: {
    email: string;
    activeView: DashboardView;
    onNavigate: (view: DashboardView) => void;
    onSignOut: () => void; })
{
    return (
        <aside className="app-sidebar" aria-label="Application sidebar">
            <div className="sidebar-brand">
                <p className="app-name">PacePilot</p>
            </div>

            <nav className="sidebar-nav" aria-label="Main navigation">
                {navigationItems.map((item) => (
                    <button key={item.view} type="button" className="sidebar-link"
                        aria-current={props.activeView === item.view ? 'page' : undefined}
                        onClick={() => props.onNavigate(item.view)}
                    >
                        <SidebarIcon path={item.iconPath}/>
                        <span>{item.label}</span>
                    </button>
                ))}
            </nav>

            <div className="sidebar-footer">
                <p className="sidebar-account-label">Signed in as</p>
                <p className="sidebar-email">{props.email}</p>

                <button type="button" className="sidebar-sign-out" onClick={props.onSignOut}>
                    <SidebarIcon path="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>
                    <span>Sign out</span>
                </button>
            </div>
        </aside>
    );
}