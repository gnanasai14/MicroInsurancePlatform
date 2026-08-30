import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useRegisterMutation, useVerifyOtpMutation, useResendOtpMutation } from '../api/userApi'
import {
  Button, Field, PasswordField, PasswordStrengthChecklist, passwordMeetsAllRules, inputClass,
} from '../components/ui'

type Step = 'form' | 'otp'

export default function RegisterPage() {
  const [step, setStep] = useState<Step>('form')
  const [form, setForm] = useState({ firstName: '', lastName: '', username: '', email: '', sms: '+1', password: '' })
  const [otp, setOtp] = useState('')
  const [maskedEmail, setMaskedEmail] = useState('')

  const [register, { isLoading: registering, error: registerError }] = useRegisterMutation()
  const [verifyOtp, { isLoading: verifying, error: otpError }] = useVerifyOtpMutation()
  const [resendOtp, { isLoading: resending, isSuccess: resent }] = useResendOtpMutation()
  const navigate = useNavigate()

  const isGmail = /^[A-Za-z0-9._%+-]+@gmail\.com$/.test(form.email)
  const passwordValid = passwordMeetsAllRules(form.password)
  const canSubmit = isGmail && passwordValid && form.firstName && form.lastName && form.username && form.sms

  async function onSubmitForm(e: React.FormEvent) {
    e.preventDefault()
    if (!canSubmit) return
    try {
      const res = await register(form).unwrap()
      setMaskedEmail(res.message.replace('Verification code sent to ', ''))
      setStep('otp')
    } catch {
      // registerError renders below
    }
  }

  async function onSubmitOtp(e: React.FormEvent) {
    e.preventDefault()
    try {
      await verifyOtp({ username: form.username, otp }).unwrap()
      navigate('/login', { state: { justVerified: true } })
    } catch {
      // otpError renders below
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-paper px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <div className="font-display text-2xl font-bold text-ink">OD·MIP</div>
          <div className="mt-1 text-sm text-ink-soft">
            {step === 'form' ? 'Set up an account in under a minute.' : 'Check your email for a code.'}
          </div>
        </div>

        {step === 'form' ? (
          <form onSubmit={onSubmitForm} className="space-y-4 rounded-2xl border border-line bg-surface p-6 shadow-sm">
            <div className="grid grid-cols-2 gap-3">
              <Field label="First name">
                <input className={inputClass} value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
              </Field>
              <Field label="Last name">
                <input className={inputClass} value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
              </Field>
            </div>
            <Field label="Username">
              <input className={inputClass} value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })} required minLength={3} />
            </Field>
            <Field label="Email (must be @gmail.com)">
              <input className={inputClass} type="email" value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="you@gmail.com" required />
              {form.email.length > 0 && !isGmail && (
                <p className="mt-1 text-xs text-danger">Email must end with @gmail.com</p>
              )}
            </Field>
            <Field label="Phone (SMS alerts)">
              <input className={inputClass} value={form.sms}
                onChange={(e) => setForm({ ...form, sms: e.target.value })} placeholder="+15551234567" required />
            </Field>
            <Field label="Password">
              <PasswordField value={form.password} onChange={(v) => setForm({ ...form, password: v })} autoComplete="new-password" />
              <PasswordStrengthChecklist password={form.password} />
            </Field>
            {registerError && (
              <p className="text-sm text-danger">
                {'data' in registerError && (registerError.data as { message?: string })?.message
                  ? (registerError.data as { message: string }).message
                  : "Couldn't create that account - check the details and try again."}
              </p>
            )}
            <Button type="submit" className="w-full" disabled={registering || !canSubmit}>
              {registering ? 'Creating account…' : 'Create account'}
            </Button>
          </form>
        ) : (
          <form onSubmit={onSubmitOtp} className="space-y-4 rounded-2xl border border-line bg-surface p-6 shadow-sm">
            <p className="text-sm text-ink-soft">
              We sent a 6-digit code to <span className="font-semibold text-ink">{maskedEmail || form.email}</span>.
              {' '}(No SMTP configured? Check user-service's console log for the code.)
            </p>
            <Field label="Verification code">
              <input
                className={inputClass + ' text-center font-mono text-lg tracking-[0.4em]'}
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="000000"
                inputMode="numeric"
                maxLength={6}
                required
                autoFocus
              />
            </Field>
            {otpError && (
              <p className="text-sm text-danger">
                {'data' in otpError && (otpError.data as { message?: string })?.message
                  ? (otpError.data as { message: string }).message
                  : "That code didn't work - try again."}
              </p>
            )}
            <Button type="submit" className="w-full" disabled={verifying || otp.length !== 6}>
              {verifying ? 'Verifying…' : 'Verify & continue'}
            </Button>
            <div className="text-center">
              <button
                type="button"
                onClick={() => resendOtp({ username: form.username })}
                disabled={resending}
                className="text-xs font-semibold text-ultra hover:underline"
              >
                {resending ? 'Sending…' : resent ? 'Code resent ✓' : "Didn't get it? Resend code"}
              </button>
            </div>
          </form>
        )}

        <p className="mt-4 text-center text-sm text-ink-soft">
          Already have an account? <Link to="/login" className="font-semibold text-ultra">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
