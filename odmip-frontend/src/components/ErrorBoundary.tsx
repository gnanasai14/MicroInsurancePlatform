import { Component, type ReactNode } from 'react'

interface Props { children: ReactNode }
interface State { error: Error | null }

/**
 * Without this, any uncaught error during render (a null field, an
 * unexpected API response shape, etc.) blanks the ENTIRE app instead of
 * just breaking the one component that hit it - the exact symptom that
 * showed up on the pricing quote page. This catches it and shows what
 * broke instead of a silent white screen.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error) {
    return { error }
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('Render error caught by ErrorBoundary:', error, info)
  }

  render() {
    if (this.state.error) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-paper px-6">
          <div className="max-w-md rounded-2xl border border-line bg-surface p-6 text-center shadow-sm">
            <div className="font-display text-lg font-semibold text-ink">Something broke on this page</div>
            <p className="mt-2 text-sm text-ink-soft">
              {this.state.error.message || 'An unexpected error occurred.'}
            </p>
            <button
              onClick={() => this.setState({ error: null })}
              className="mt-4 rounded-xl bg-flare px-4 py-2 text-sm font-semibold text-white"
            >
              Try again
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
